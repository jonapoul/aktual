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
package aktual.budget.db.test

import kotlin.time.Instant
import kotlin.uuid.Uuid

internal val BANK_ID = Uuid.parse("3df1f75a-fcc6-4e5f-a5a1-38ae7aa145eb")
internal val LAST_SYNC = Instant.parse("2025-05-12T01:23:45.678Z")
internal val LAST_RECONCILED = Instant.parse("2025-05-11T01:23:45.678Z")
