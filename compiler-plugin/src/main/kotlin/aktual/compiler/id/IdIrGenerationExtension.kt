package aktual.compiler.id

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin.GeneratedByPlugin
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.nonDispatchParameters
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.util.OperatorNameConventions.COMPARE_TO
import org.jetbrains.kotlin.util.OperatorNameConventions.TO_STRING

internal class IdIrGenerationExtension : IrGenerationExtension {
  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    moduleFragment.transformChildrenVoid(
      transformer =
        object : IrElementTransformerVoid() {
          override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
            if (declaration.origin == GeneratedByPlugin(IdPluginKey)) {
              declaration.body = buildBody(pluginContext, declaration)
            }
            return super.visitSimpleFunction(declaration)
          }
        }
    )
  }

  @OptIn(UnsafeDuringIrConstructionAPI::class)
  private fun buildBody(pluginContext: IrPluginContext, function: IrSimpleFunction) =
    DeclarationIrBuilder(pluginContext, function.symbol).irBlockBody {
      val owner = function.parentAsClass
      val valueProperty = owner.declarations.filterIsInstance<IrProperty>().single()
      val getter = valueProperty.getter!!
      val thisParam = function.dispatchReceiverParameter!!

      when (function.name) {
        TO_STRING -> {
          +irReturn(irCall(getter.symbol).apply { dispatchReceiver = irGet(thisParam) })
        }

        COMPARE_TO -> {
          val otherParam = function.nonDispatchParameters.single()
          val thisValue = irCall(getter.symbol).apply { dispatchReceiver = irGet(thisParam) }
          val otherValue = irCall(getter.symbol).apply { dispatchReceiver = irGet(otherParam) }
          val compareToOnProperty =
            getter.returnType.classOrFail.owner.functions.single {
              it.name == COMPARE_TO && it.nonDispatchParameters.size == 1
            }
          +irReturn(
            irCall(compareToOnProperty.symbol).apply {
              dispatchReceiver = thisValue
              arguments[1] = otherValue
            }
          )
        }

        else -> Unit
      }
    }
}
