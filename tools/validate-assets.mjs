import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const assetsRoot = path.join(root, "src", "main", "resources", "assets");
const namespace = "modularmachinery";
const modAssets = path.join(assetsRoot, namespace);

const blockIds = [
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

const itemIds = [
    ...blockIds,
    "itemblueprint",
    "itemmodularium",
    "itemconstructtool",
    "machine_projector"
];

const problems = [];
const visitedModels = new Set();

function exists(file) {
    return fs.existsSync(file);
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

function modelPath(ref) {
    const [ns, id] = splitResource(ref);
    return path.join(assetsRoot, ns, "models", `${id}.json`);
}

function texturePath(ref) {
    const [ns, id] = splitResource(ref);
    return path.join(assetsRoot, ns, "textures", `${id}.png`);
}

function checkRegisteredFiles() {
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

function checkBlockstates() {
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

function checkModel(ref) {
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

    const json = readJson(file);
    if (!json) {
        return;
    }
    if (json.parent && !json.parent.startsWith("minecraft:")) {
        checkModel(json.parent);
    }
    if (json.textures) {
        for (const [textureKey, textureRef] of Object.entries(json.textures)) {
            if (typeof textureRef !== "string" || textureRef.startsWith("#")) {
                continue;
            }
            const fileRef = texturePath(textureRef);
            if (!exists(fileRef)) {
                problems.push(`Missing texture ${textureRef} for ${ref}#${textureKey}`);
            }
        }
    }
    if (json.overrides) {
        for (const override of json.overrides) {
            if (override.model) {
                checkModel(override.model);
            }
        }
    }
}

function checkItemModels() {
    for (const id of itemIds) {
        checkModel(`${namespace}:item/${id}`);
    }
}

checkRegisteredFiles();
checkBlockstates();
checkItemModels();

if (problems.length > 0) {
    for (const problem of problems) {
        console.error(problem);
    }
    process.exit(1);
}

console.log(`Asset validation passed (${visitedModels.size} models checked).`);
