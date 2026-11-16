const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_1";
const BOB_UID = "bob_uid_2";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

const ts = () => new Date();

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) await testEnv.cleanup();
});

beforeEach(async () => {
  if (testEnv) await testEnv.clearFirestore();
});

test("Unauthenticated user cannot read or list worlds or users", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("worlds").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("conversations").get());
  await assertFails(unauthDb.collection("marketplace_items").get());
});

test("User profile PII isolation: Bob cannot read or write Alice profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      email: "alice@aetherx.io",
      createdAt: ts(),
      updatedAt: ts(),
    })
  );

  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({ displayName: "Hacked" })
  );
});

test("Shadow Update & Identity Spoofing rejected on worlds", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Spoofed ownerId rejected
  await assertFails(
    aliceDb.collection("worlds").doc("world_spoof").set({
      ownerId: BOB_UID,
      memberUids: [ALICE_UID, BOB_UID],
      name: "Spoof World",
      colorHex: "#22D3EE",
      category: "Architecture",
      createdAt: ts(),
      updatedAt: ts(),
    })
  );

  // Ghost field rejected
  await assertFails(
    aliceDb.collection("worlds").doc("world_ghost").set({
      ownerId: ALICE_UID,
      memberUids: [ALICE_UID],
      name: "Ghost World",
      colorHex: "#22D3EE",
      category: "Architecture",
      isAdminOverride: true,
      createdAt: ts(),
      updatedAt: ts(),
    })
  );

  // Valid world creation succeeds
  await assertSucceeds(
    aliceDb.collection("worlds").doc("world_1").set({
      ownerId: ALICE_UID,
      memberUids: [ALICE_UID],
      name: "Obsidian Core",
      description: "Primary universe node",
      colorHex: "#22D3EE",
      category: "Architecture",
      createdAt: ts(),
      updatedAt: ts(),
    })
  );

  // Query aligned with memberUids succeeds; unfiltered query fails
  await assertSucceeds(
    aliceDb.collection("worlds").where("memberUids", "array-contains", ALICE_UID).get()
  );
  await assertFails(aliceDb.collection("worlds").get());
});

test("Parent Gate: Project requires existing World; cross-user blocked", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  // Missing parent world fails
  await assertFails(
    aliceDb.collection("projects").doc("proj_orphan").set({
      ownerId: ALICE_UID,
      memberUids: [ALICE_UID],
      worldId: "missing_world",
      title: "Orphan Project",
      status: "active",
      progress: 10,
      createdAt: ts(),
      updatedAt: ts(),
    })
  );

  // Create world first
  await assertSucceeds(
    aliceDb.collection("worlds").doc("world_p").set({
      ownerId: ALICE_UID,
      memberUids: [ALICE_UID],
      name: "Nexus Prime",
      colorHex: "#38BDF8",
      category: "Product",
      createdAt: ts(),
      updatedAt: ts(),
    })
  );

  // Valid project succeeds
  await assertSucceeds(
    aliceDb.collection("projects").doc("proj_1").set({
      ownerId: ALICE_UID,
      memberUids: [ALICE_UID],
      worldId: "world_p",
      title: "Quantum UI",
      status: "active",
      progress: 45,
      createdAt: ts(),
      updatedAt: ts(),
    })
  );

  // Bob cannot read Alice's project
  await assertFails(bobDb.collection("projects").doc("proj_1").get());
});

test("Conversations, AgentRuns, Activities, Notifications & Nexus Market security bounds", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  // Conversation
  await assertSucceeds(
    aliceDb.collection("conversations").doc("conv_1").set({
      ownerId: ALICE_UID,
      agentId: "architect",
      title: "System Design",
      prompt: "Design Aether X",
      responseSummary: "Modular architecture ready.",
      isVoice: false,
      createdAt: ts(),
      updatedAt: ts(),
    })
  );
  await assertFails(bobDb.collection("conversations").doc("conv_1").get());

  // Activity is append-only (no update)
  await assertSucceeds(
    aliceDb.collection("activities").doc("act_1").set({
      ownerId: ALICE_UID,
      memberUids: [ALICE_UID],
      actorName: "Alice",
      action: "Created Obsidian Core",
      category: "world",
      createdAt: ts(),
    })
  );
  await assertFails(
    aliceDb.collection("activities").doc("act_1").update({ action: "Tampered" })
  );

  // Public Marketplace item readable by Bob and Bob can favorite, but cannot overwrite title
  await assertSucceeds(
    aliceDb.collection("marketplace_items").doc("mkt_1").set({
      ownerId: ALICE_UID,
      creatorName: "Alice",
      title: "Cyan Orbital Kit",
      description: "3D node preset",
      category: "World Kit",
      coverKey: "cosmos",
      favoriteUids: [],
      isPublic: true,
      createdAt: ts(),
      updatedAt: ts(),
    })
  );
  await assertSucceeds(
    bobDb.collection("marketplace_items").where("isPublic", "==", true).get()
  );
  await assertSucceeds(
    bobDb.collection("marketplace_items").doc("mkt_1").update({
      favoriteUids: [BOB_UID],
      updatedAt: ts(),
    })
  );
  await assertFails(
    bobDb.collection("marketplace_items").doc("mkt_1").update({
      title: "Stolen Title",
      updatedAt: ts(),
    })
  );
});
