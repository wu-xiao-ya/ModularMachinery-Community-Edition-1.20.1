import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const assetsRoot = path.join(root, "src", "main", "resources", "assets");
const namespace = "modularmachinery";
const modAssets = path.join(assetsRoot, namespace);
const sourceRoot = path.join(root, "src", "port", "java");

const fallbackBlockIds = [
    "blockcontroller",
    "blockfactorycontroller",
    "blockcasing",
    "blockinputbus",
    "blockoutputbus",
    "blockfluidinputhatch",
    "blockfluidoutputhatch",
    "blockfluidprocessorhatch",
    "blockenergyinputhatch",
    "blockenergyoutputhatch",
    "blocksmartinterface",
    "blockparallelcontroller",
    "blockupgradebus"
];

const fallbackItemIds = [
    ...fallbackBlockIds,
    "itemblueprint",
    "itemmodularium",
    "itemconstructtool",
    "machine_projector"
];

const guiTextures = new Map([
    ["guicontroller_large", [256, 256]],
    ["guifactory", [280, 213]],
    ["guifactoryelements", [256, 256]],
    ["guibar", [256, 256]],
    ["guismartinterface", [256, 256]],
    ["guiupgradebus", [256, 256]],
    ["inventory_tiny", [256, 256]],
    ["inventory_small", [256, 256]],
    ["inventory_normal", [256, 256]],
    ["inventory_reinforced", [256, 256]],
    ["inventory_big", [256, 256]],
    ["inventory_huge", [256, 256]],
    ["inventory_ludicrous", [256, 256]]
]);

const statefulBlocks = new Map([
    ["blockcasing", { property: "casing", values: ["plain", "vent", "firebox", "gearbox", "reinforced", "circuitry"] }],
    ["blockinputbus", { property: "size", values: ["tiny", "small", "normal", "reinforced", "big", "huge", "ludicrous"] }],
    ["blockoutputbus", { property: "size", values: ["tiny", "small", "normal", "reinforced", "big", "huge", "ludicrous"] }],
    ["blockfluidinputhatch", { property: "size", values: ["tiny", "small", "normal", "reinforced", "big", "huge", "ludicrous", "vacuum"] }],
    ["blockfluidoutputhatch", { property: "size", values: ["tiny", "small", "normal", "reinforced", "big", "huge", "ludicrous", "vacuum"] }],
    ["blockfluidprocessorhatch", { property: "size", values: ["tiny", "small", "normal", "reinforced", "big", "huge", "ludicrous", "vacuum"] }],
    ["blockenergyinputhatch", { property: "size", values: ["tiny", "small", "normal", "reinforced", "big", "huge", "ludicrous", "ultimate"] }],
    ["blockenergyoutputhatch", { property: "size", values: ["tiny", "small", "normal", "reinforced", "big", "huge", "ludicrous", "ultimate"] }],
    ["blocksmartinterface", { property: "type", values: ["number", "string"] }],
    ["blockparallelcontroller", { property: "type", values: ["normal", "reinforced", "elite", "super", "ultimate"] }],
    ["blockupgradebus", { property: "type", values: ["normal", "reinforced", "elite", "super", "ultimate"] }]
]);

const legacyRefs = new Map([
    ["minecraft:stonebrick", "minecraft:stone_bricks"],
    ["minecraft:stonebrick@0", "minecraft:stone_bricks"],
    ["minecraft:grass", "minecraft:grass_block"],
    ["minecraft:wool", "#minecraft:wool"],
    ["minecraft:dye", "modern dye item ids, e.g. minecraft:cocoa_beans for old metadata 3"],
    ["minecraft:dye@3", "minecraft:cocoa_beans"],
    ["minecraft:glass@0", "minecraft:glass"],
    ["minecraft:quartz_stairs@0", "minecraft:quartz_stairs[facing=east] or another explicit 1.21 blockstate"],
    ["minecraft:quartz_stairs@1", "minecraft:quartz_stairs[facing=west] or another explicit 1.21 blockstate"]
]);

const knownMinecraftParents = new Set([
    "minecraft:block/block",
    "minecraft:block/cube",
    "minecraft:block/cube_all",
    "minecraft:block/orientable",
    "minecraft:block/orientable_with_bottom",
    "minecraft:block/with_generated",
    "minecraft:item/generated",
    "minecraft:item/handheld",
    "minecraft:builtin/entity"
]);

const problems = [];
const visitedModels = new Set();
const modelCache = new Map();
const checkedPngs = new Set();

function exists(file) {
    return fs.existsSync(file);
}

function readText(file) {
    try {
        return fs.readFileSync(file, "utf8");
    } catch (error) {
        problems.push(`Unable to read ${relative(file)} (${error.message})`);
        return "";
    }
}

function readJson(file) {
    try {
        return JSON.parse(fs.readFileSync(file, "utf8"));
    } catch (error) {
        problems.push(`Invalid JSON: ${relative(file)} (${error.message})`);
        return null;
    }
}

function relative(file) {
    return path.relative(root, file).replaceAll("\\", "/");
}

function splitResource(ref, fallbackNamespace = namespace) {
    const separator = ref.indexOf(":");
    if (separator >= 0) {
        return [ref.slice(0, separator), ref.slice(separator + 1)];
    }
    return [fallbackNamespace, ref];
}

function resourceRef(ns, id) {
    return `${ns}:${id.replaceAll("\\", "/")}`;
}

function modelPath(ref) {
    const [ns, id] = splitResource(ref);
    return path.join(assetsRoot, ns, "models", `${id}.json`);
}

function texturePath(ref) {
    const [ns, id] = splitResource(ref);
    return path.join(assetsRoot, ns, "textures", `${id}.png`);
}

function textureResourcePath(ref) {
    const [ns, id] = splitResource(ref);
    const cleanId = id
            .replace(/^textures\//u, "")
            .replace(/\.png$/u, "");
    return path.join(assetsRoot, ns, "textures", `${cleanId}.png`);
}

function registeredIds() {
    const blockFile = path.join(sourceRoot, "hellfirepvp", "modularmachinery", "port", "registry", "MmceBlocks.java");
    const itemFile = path.join(sourceRoot, "hellfirepvp", "modularmachinery", "port", "registry", "MmceItems.java");
    const blockIds = idsFromRegisterCalls(blockFile, "BLOCKS.register");
    const directItemIds = idsFromRegisterCalls(itemFile, "ITEMS.register");
    const blocks = blockIds.length > 0 ? blockIds : fallbackBlockIds;
    const items = directItemIds.length > 0
            ? [...new Set([...blocks, ...fallbackItemIds, ...directItemIds])]
            : fallbackItemIds;
    return { blockIds: blocks, itemIds: items };
}

function idsFromRegisterCalls(file, call) {
    if (!exists(file)) {
        problems.push(`Missing registry source: ${relative(file)}`);
        return [];
    }
    const regex = new RegExp(`${escapeRegex(call)}\\(\\s*"([^"]+)"`, "g");
    const text = readText(file);
    const ids = [];
    for (const match of text.matchAll(regex)) {
        ids.push(match[1]);
    }
    return [...new Set(ids)];
}

function escapeRegex(value) {
    return value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

function checkRegisteredFiles(blockIds, itemIds) {
    for (const id of blockIds) {
        const blockstate = path.join(modAssets, "blockstates", `${id}.json`);
        if (!exists(blockstate)) {
            problems.push(`Missing blockstate for ${namespace}:${id}`);
        }
    }
    for (const id of itemIds) {
        const model = path.join(modAssets, "models", "item", `${id}.json`);
        if (!exists(model)) {
            problems.push(`Missing item model for ${namespace}:${id}`);
        }
    }
}

function referencedBlockstateModels(blockstateJson) {
    const refs = [];
    if (blockstateJson.variants) {
        for (const value of Object.values(blockstateJson.variants)) {
            if (Array.isArray(value)) {
                for (const variant of value) {
                    if (variant.model) {
                        refs.push(variant.model);
                    }
                }
            } else if (value?.model) {
                refs.push(value.model);
            }
        }
    }
    if (blockstateJson.multipart) {
        for (const part of blockstateJson.multipart) {
            const apply = part.apply;
            if (Array.isArray(apply)) {
                for (const variant of apply) {
                    if (variant.model) {
                        refs.push(variant.model);
                    }
                }
            } else if (apply?.model) {
                refs.push(apply.model);
            }
        }
    }
    return refs;
}

function checkBlockstates(blockIds) {
    for (const id of blockIds) {
        const blockstate = path.join(modAssets, "blockstates", `${id}.json`);
        if (!exists(blockstate)) {
            continue;
        }
        const json = readJson(blockstate);
        if (!json) {
            continue;
        }
        for (const ref of referencedBlockstateModels(json)) {
            const file = modelPath(ref);
            if (!exists(file)) {
                problems.push(`Missing model ${ref} referenced by blockstate ${namespace}:${id}`);
            } else {
                checkModel(ref);
            }
        }
    }
}

function checkAllBlockstates() {
    const blockstatesDir = path.join(modAssets, "blockstates");
    if (!exists(blockstatesDir)) {
        problems.push(`Missing blockstates directory for ${namespace}`);
        return;
    }
    for (const file of listFiles(blockstatesDir, ".json")) {
        const json = readJson(file);
        if (!json) {
            continue;
        }
        for (const ref of referencedBlockstateModels(json)) {
            const modelFile = modelPath(ref);
            if (!exists(modelFile)) {
                problems.push(`Missing model ${ref} referenced by blockstate ${relative(file)}`);
            } else {
                checkModel(ref);
            }
        }
    }
}

function modelJson(ref) {
    const file = modelPath(ref);
    if (!exists(file)) {
        problems.push(`Missing model file ${ref}`);
        return null;
    }
    const key = path.resolve(file).toLowerCase();
    if (!modelCache.has(key)) {
        modelCache.set(key, readJson(file));
    }
    return modelCache.get(key);
}

function checkModel(ref, parentOnly = false) {
    const file = modelPath(ref);
    if (!exists(file)) {
        problems.push(`Missing model file ${ref}`);
        return;
    }
    const key = path.resolve(file).toLowerCase();
    if (visitedModels.has(key)) {
        return;
    }
    visitedModels.add(key);

    const json = modelJson(ref);
    if (!json) {
        return;
    }
    if (json.parent) {
        if (json.parent.startsWith("minecraft:")) {
            checkMinecraftParent(ref, json.parent);
        } else {
            checkModel(json.parent, true);
        }
    }
    checkTextureEntries(ref, json);
    if (!parentOnly) {
        checkFaceTextureReferences(ref);
    }
    if (json.overrides) {
        for (const override of json.overrides) {
            if (override.model) {
                checkModel(override.model);
            }
        }
    }
}

function checkMinecraftParent(ref, parent) {
    if (!knownMinecraftParents.has(parent)) {
        problems.push(`Unexpected minecraft parent ${parent} used by ${ref}`);
    }
}

function checkTextureEntries(ref, json) {
    if (!json.textures) {
        return;
    }
    for (const [textureKey, textureRef] of Object.entries(json.textures)) {
        if (typeof textureRef !== "string" || textureRef.startsWith("#")) {
            continue;
        }
        const fileRef = texturePath(textureRef);
        if (!exists(fileRef)) {
            problems.push(`Missing texture ${textureRef} for ${ref}#${textureKey}`);
        } else {
            checkPng(fileRef, null, `texture ${textureRef} for ${ref}#${textureKey}`);
        }
    }
}

function checkFaceTextureReferences(ref, json) {
    const elementSource = modelWithElements(ref);
    if (!elementSource) {
        return;
    }
    for (const element of elementSource.json.elements) {
        if (!element?.faces) {
            continue;
        }
        for (const [face, faceData] of Object.entries(element.faces)) {
            const texture = faceData?.texture;
            if (typeof texture === "string" && texture.startsWith("#")) {
                const resolved = resolveTexture(ref, texture.slice(1));
                if (!resolved) {
                    problems.push(`Missing texture key ${texture} used by ${elementSource.ref} through ${ref} face ${face}`);
                }
            }
        }
    }
}

function modelWithElements(ref) {
    const json = modelJson(ref);
    if (!json) {
        return null;
    }
    if (Array.isArray(json.elements)) {
        return { ref, json };
    }
    if (json.parent && !json.parent.startsWith("minecraft:")) {
        return modelWithElements(json.parent);
    }
    return null;
}

function resolveTexture(ref, key, seen = new Set()) {
    const marker = `${ref}#${key}`;
    if (seen.has(marker)) {
        problems.push(`Cyclic texture reference at ${marker}`);
        return null;
    }
    seen.add(marker);

    const value = textureSlotValue(ref, key);
    if (typeof value !== "string") {
        return null;
    }
    if (value.startsWith("#")) {
        return resolveTexture(ref, value.slice(1), seen);
    }
    return value;
}

function textureSlotValue(ref, key) {
    const json = modelJson(ref);
    if (!json) {
        return null;
    }
    const value = json.textures?.[key];
    if (typeof value === "string") {
        return value;
    }
    if (json.parent && !json.parent.startsWith("minecraft:")) {
        return textureSlotValue(json.parent, key);
    }
    return null;
}

function checkItemModels(itemIds) {
    for (const id of itemIds) {
        checkModel(`${namespace}:item/${id}`);
    }
}

function checkStatefulItemOverrides(itemIds) {
    for (const id of itemIds) {
        const stateful = statefulBlocks.get(id);
        if (!stateful) {
            continue;
        }
        const itemModelRef = `${namespace}:item/${id}`;
        const json = modelJson(itemModelRef);
        if (!json) {
            continue;
        }
        const seenVariants = new Set();
        for (const override of json.overrides ?? []) {
            const variant = override?.predicate?.[`${namespace}:variant`];
            if (typeof variant !== "number") {
                continue;
            }
            seenVariants.add(variant);
            const value = stateful.values[variant];
            if (value === undefined) {
                problems.push(`Unexpected ${namespace}:variant ${variant} in ${itemModelRef}; ${id} only has ${stateful.values.length} values`);
                continue;
            }
            const expectedModel = `${namespace}:item/${id}_${value}`;
            if (override.model !== expectedModel) {
                problems.push(`Unexpected model ${override.model} for ${itemModelRef} variant ${variant}; expected ${expectedModel}`);
            }
            checkModel(expectedModel);
        }
        for (let index = 0; index < stateful.values.length; index++) {
            if (!seenVariants.has(index)) {
                problems.push(`Missing ${namespace}:variant ${index} override for ${itemModelRef}`);
            }
        }
        checkBlockstateVariantValues(id, stateful);
    }
}

function checkBlockstateVariantValues(id, stateful) {
    const blockstate = path.join(modAssets, "blockstates", `${id}.json`);
    if (!exists(blockstate)) {
        return;
    }
    const json = readJson(blockstate);
    if (!json?.variants) {
        return;
    }
    const expectedKeys = new Set(stateful.values.map(value => `${stateful.property}=${value}`));
    for (const key of Object.keys(json.variants)) {
        if (!expectedKeys.has(key)) {
            problems.push(`Unexpected blockstate variant ${key} in ${relative(blockstate)}; expected ${[...expectedKeys].join(", ")}`);
        }
    }
    for (const key of expectedKeys) {
        if (!Object.hasOwn(json.variants, key)) {
            problems.push(`Missing blockstate variant ${key} in ${relative(blockstate)}`);
        }
    }
}

function checkAllNamespaceModels() {
    const modelsDir = path.join(modAssets, "models");
    if (!exists(modelsDir)) {
        problems.push(`Missing models directory for ${namespace}`);
        return;
    }
    for (const file of listFiles(modelsDir, ".json")) {
        const rel = path.relative(modelsDir, file).replaceAll("\\", "/").replace(/\.json$/u, "");
        checkModel(resourceRef(namespace, rel));
    }
}

function listFiles(directory, extension) {
    const files = [];
    for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
        const fullPath = path.join(directory, entry.name);
        if (entry.isDirectory()) {
            files.push(...listFiles(fullPath, extension));
        } else if (entry.isFile() && entry.name.endsWith(extension)) {
            files.push(fullPath);
        }
    }
    return files;
}

function checkJavaTextureReferences() {
    if (!exists(sourceRoot)) {
        return;
    }
    for (const file of listFiles(sourceRoot, ".java")) {
        const text = readText(file);
        const resourceRegex = /ResourceLocation\.fromNamespaceAndPath\(\s*ModularMachineryNeoForge\.MODID\s*,\s*"([^"]+)"\s*\)/g;
        for (const match of text.matchAll(resourceRegex)) {
            if (match[1].startsWith("textures/")) {
                checkTextureResource(`${namespace}:${match[1]}`, `Java reference in ${relative(file)}`);
            }
        }

        const guiTextureRegex = /\btexture\("([^"]+)"\)/g;
        for (const match of text.matchAll(guiTextureRegex)) {
            const name = match[1];
            checkGuiTexture(name, `texture("${name}") in ${relative(file)}`);
        }
    }
    for (const name of guiTextures.keys()) {
        checkGuiTexture(name, "MmceMachineScreen background set");
    }
}

function checkDefaultDataReferences(blockIds, itemIds) {
    const knownBlocks = new Set([...blockIds, ...statefulBlocks.keys()]);
    const knownItems = new Set(itemIds);
    const dataDirs = [
        path.join(modAssets, "default_machinery"),
        path.join(modAssets, "default_variables"),
        path.join(modAssets, "default_recipes"),
        path.join(modAssets, "recipes")
    ];
    for (const directory of dataDirs) {
        if (!exists(directory)) {
            continue;
        }
        for (const file of listFiles(directory, ".json")) {
            const json = readJson(file);
            if (!json) {
                continue;
            }
            walkJson(json, (value, key) => {
                if (typeof value !== "string") {
                    return;
                }
                checkLegacyReference(value, file);
                if (key === "elements" || key === "item" || key === "itemId" || key === "item-id" || key === "item_id") {
                    checkDescriptor(value, key, file, knownBlocks, knownItems);
                }
            });
        }
    }
}

function walkJson(value, visitor, key = "") {
    visitor(value, key);
    if (Array.isArray(value)) {
        for (const child of value) {
            walkJson(child, visitor, key);
        }
    } else if (value && typeof value === "object") {
        for (const [childKey, child] of Object.entries(value)) {
            walkJson(child, visitor, childKey);
        }
    }
}

function checkLegacyReference(value, file) {
    const normalized = value.trim();
    if (legacyRefs.has(normalized)) {
        problems.push(`Legacy reference ${normalized} in ${relative(file)}; use ${legacyRefs.get(normalized)}`);
        return;
    }
    if (/^minecraft:[a-z0-9_./-]+@[0-9]+$/u.test(normalized)) {
        problems.push(`Legacy minecraft metadata reference ${normalized} in ${relative(file)}; use an explicit 1.21 id or blockstate`);
    }
}

function checkDescriptor(value, key, file, knownBlocks, knownItems) {
    if (value.startsWith("#") || value.startsWith("ore:") || !value.includes(":")) {
        return;
    }
    if (key === "elements") {
        checkBlockDescriptor(value, file, knownBlocks);
    } else {
        checkItemDescriptor(value, file, knownItems);
    }
}

function checkBlockDescriptor(value, file, knownBlocks) {
    const { ns, path: id, meta } = parseStateDescriptor(value);
    if (ns !== namespace) {
        return;
    }
    if (!knownBlocks.has(id)) {
        problems.push(`Unknown ${namespace} block element ${value} in ${relative(file)}`);
        return;
    }
    if (meta === null) {
        return;
    }
    const stateful = statefulBlocks.get(id);
    if (!stateful) {
        problems.push(`Unexpected metadata reference ${value} in ${relative(file)}; ${namespace}:${id} is not stateful`);
        return;
    }
    if (meta < 0 || meta >= stateful.values.length) {
        problems.push(`Metadata ${meta} out of range for ${namespace}:${id} in ${relative(file)}; expected 0-${stateful.values.length - 1}`);
    }
}

function checkItemDescriptor(value, file, knownItems) {
    const { ns, path: id } = parseStateDescriptor(value);
    if (ns !== namespace || knownItems.has(id)) {
        return;
    }
    problems.push(`Unknown ${namespace} item ${value} in ${relative(file)}`);
}

function parseStateDescriptor(value) {
    const withoutState = value.replace(/\[[^\]]*\]$/u, "");
    const metaIndex = withoutState.lastIndexOf("@");
    const base = metaIndex < 0 ? withoutState : withoutState.slice(0, metaIndex);
    const meta = metaIndex < 0 ? null : Number.parseInt(withoutState.slice(metaIndex + 1), 10);
    const [ns, id] = splitResource(base);
    return { ns, path: id, meta: Number.isNaN(meta) ? null : meta };
}

function checkTextureResource(ref, source) {
    const file = textureResourcePath(ref);
    if (!exists(file)) {
        problems.push(`Missing texture ${ref} referenced by ${source}`);
        return;
    }
    checkPng(file, null, `${ref} referenced by ${source}`);
}

function checkGuiTexture(name, source) {
    const expected = guiTextures.get(name) ?? null;
    const file = path.join(modAssets, "textures", "gui", `${name}.png`);
    if (!exists(file)) {
        problems.push(`Missing GUI texture textures/gui/${name}.png referenced by ${source}`);
        return;
    }
    checkPng(file, expected, `GUI texture textures/gui/${name}.png referenced by ${source}`);
}

function checkPng(file, expectedSize, source) {
    const cacheKey = `${path.resolve(file).toLowerCase()}|${expectedSize?.join("x") ?? ""}`;
    if (checkedPngs.has(cacheKey)) {
        return;
    }
    checkedPngs.add(cacheKey);
    let bytes;
    try {
        bytes = fs.readFileSync(file);
    } catch (error) {
        problems.push(`Unable to read PNG ${relative(file)} for ${source} (${error.message})`);
        return;
    }
    const signature = "89504e470d0a1a0a";
    if (bytes.length < 24 || bytes.subarray(0, 8).toString("hex") !== signature) {
        problems.push(`Invalid PNG header ${relative(file)} for ${source}`);
        return;
    }
    const width = bytes.readUInt32BE(16);
    const height = bytes.readUInt32BE(20);
    if (expectedSize && (width !== expectedSize[0] || height !== expectedSize[1])) {
        problems.push(`Unexpected PNG size ${relative(file)} for ${source}: ${width}x${height}, expected ${expectedSize[0]}x${expectedSize[1]}`);
    }
}

const { blockIds, itemIds } = registeredIds();
checkRegisteredFiles(blockIds, itemIds);
checkBlockstates(blockIds);
checkAllBlockstates();
checkItemModels(itemIds);
checkStatefulItemOverrides(itemIds);
checkAllNamespaceModels();
checkJavaTextureReferences();
checkDefaultDataReferences(blockIds, itemIds);

if (problems.length > 0) {
    for (const problem of problems) {
        console.error(problem);
    }
    process.exit(1);
}

console.log(`Asset validation passed (${visitedModels.size} models, ${checkedPngs.size} PNG checks).`);
