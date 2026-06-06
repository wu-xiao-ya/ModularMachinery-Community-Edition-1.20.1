const MMCE = Java.loadClass('hellfirepvp.modularmachinery.port.integration.kubejs.MmceKubeJSBindings')

MMCE.clearScriptDefinitions()

MMCE.machine('kubejs:demo_alloy_furnace', 'KubeJS Demo Alloy Furnace')
  .part(-1, 0, 0, 'modularmachinery:blockinputbus')
  .part(1, 0, 0, 'modularmachinery:blockoutputbus')
  .part(0, 0, 1, 'modularmachinery:blockenergyinputhatch')
  .build()

MMCE.adapter('demo_furnace_adapter', 'kubejs:demo_alloy_furnace', 'minecraft:furnace')
  .durationModifier(0, 40)
  .itemInputModifier(1, 3.0)
  .itemOutputModifier(1, 3.0)
  .build()

MMCE.recipe('kubejs:demo_alloy_furnace_modularium', 'kubejs:demo_alloy_furnace', 80)
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

MMCE.recipe('kubejs:demo_repair_tool', 'kubejs:demo_alloy_furnace', 40)
  .energyInput(10)
  .itemInput('minecraft:iron_pickaxe', 1).consumeDurability(8)
  .itemInput('minecraft:redstone', 1).nbt('{"custom_model":1}').triggerTime(20)
  .randomItemOutput(['minecraft:iron_nugget', 'minecraft:redstone'], 1).chance(0.75).ignoreOutputCheck(true)
  .build()

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
