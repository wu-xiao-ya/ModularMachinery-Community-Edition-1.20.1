const MMCE = Java.loadClass('hellfirepvp.modularmachinery.port.integration.kubejs.MmceKubeJSBindings')
const MmceEvents = MMCE.events()

// KubeJS reload clears MMCE definitions from KubeJS scripts automatically.
// Do not call MMCE.clearScriptDefinitions() in server_scripts.
const MACHINE = 'kubejs:demo_alloy_furnace'
const ALLOY_RECIPE = 'kubejs:demo_alloy_furnace_modularium'
const STARTUP_BOOST = MMCE.modifier('duration', '', 0.75, 1)

MMCE.machine(MACHINE, 'KubeJS Demo Alloy Furnace')
  .part(-1, 0, 0, 'modularmachinery:blockinputbus')
  .part(1, 0, 0, 'modularmachinery:blockoutputbus')
  .part(0, 0, 1, 'modularmachinery:blockenergyinputhatch')
  .build()

MMCE.adapter('demo_furnace_adapter', MACHINE, 'minecraft:furnace')
  .durationModifier(0, 40)
  .itemInputModifier(1, 3.0)
  .itemOutputModifier(1, 3.0)
  .build()

MMCE.recipe(ALLOY_RECIPE, MACHINE, 80)
  .parallelized(true)
  .energyInput(20)
  .itemInput('minecraft:iron_ingot', 1)
  .itemInput('minecraft:gold_ingot', 1)
  .ingredientArrayInput(['minecraft:coal', 'minecraft:charcoal'], 1)
  .catalystInput('minecraft:redstone', 1).chance(0.5)
  .fluidPerTickInput('minecraft:water', 10)
  .itemOutput('modularmachinery:itemmodularium', 1)
  .startCommand('say MMCE KubeJS recipe started')
  .processingCommand('say MMCE KubeJS recipe ticking', 20)
  .finishCommand('say MMCE KubeJS recipe finished')
  .build()

MMCE.recipe('kubejs:demo_repair_tool', MACHINE, 40)
  .energyInput(10)
  .itemInput('minecraft:iron_pickaxe', 1).consumeDurability(8)
  .itemInput('minecraft:redstone', 1).nbt('{"custom_model":1}').triggerTime(20)
  .randomItemOutput(['minecraft:iron_nugget', 'minecraft:redstone'], 1).chance(0.75).ignoreOutputCheck(true)
  .onFinish(event => {
    event.removeModifier('demo_startup_boost')
  })
  .build()

MmceEvents.onStructureFormedKeyed(MACHINE, 'demo_startup_boost', event => {
  event.addModifier('demo_startup_boost', STARTUP_BOOST)
})

MmceEvents.onRecipeFinishKeyed(ALLOY_RECIPE, 'demo_cleanup', event => {
  event.removeModifier('demo_startup_boost')
})

const outputUpgrade = MMCE.upgradeStack('minecraft:paper', 1)
  .modifier('item', 'output', 1, 2.0, false, true)
  .build()

ServerEvents.recipes(event => {
  const modularium = MMCE.id('itemmodularium').toString()
  event.shaped(modularium, ['III', 'IRI', 'III'], {
    I: 'minecraft:iron_ingot',
    R: 'minecraft:redstone'
  })

  event.shaped(outputUpgrade, ['MPM'], {
    M: modularium,
    P: 'minecraft:paper'
  })
})
