/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package actual.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName

internal val ACTUAL_RESPONSE = ClassName("actual.api.client", "ActualResponse")

internal val STRING_VALUE = StringValue::class.asClassName()

private val OKHTTP_RESPONSEBODY = ClassName("okhttp3", "ResponseBody")
private val RETROFIT_RESPONSE = ClassName("retrofit2", "Response")
internal val RESPONSE_RESPONSEBODY = RETROFIT_RESPONSE.parameterizedBy(OKHTTP_RESPONSEBODY)

internal val ADAPTED = MemberName("actual.api.client", "adapted")
