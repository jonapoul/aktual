package aktual.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName

internal val ACTUAL_RESPONSE = ClassName("aktual.api.client", "AktualResponse")

internal val STRING_VALUE = StringValue::class.asClassName()

private val OKHTTP_RESPONSEBODY = ClassName(packageName = "okhttp3", "ResponseBody")
private val RETROFIT_RESPONSE = ClassName(packageName = "retrofit2", "Response")
internal val RESPONSE_RESPONSEBODY = RETROFIT_RESPONSE.parameterizedBy(OKHTTP_RESPONSEBODY)

internal val ADAPTED = MemberName(packageName = "aktual.api.client", simpleName = "adapted")
