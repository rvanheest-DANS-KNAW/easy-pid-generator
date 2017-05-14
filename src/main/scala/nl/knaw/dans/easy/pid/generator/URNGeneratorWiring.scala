/**
 * Copyright (C) 2015 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.easy.pid.generator

import java.sql.Connection

import nl.knaw.dans.easy.pid.{ PidType, PropertiesComponent, URN }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

import scala.util.Try

trait URNGeneratorWiring {
  this: PropertiesComponent =>

  val urns: URNGenerator

  trait URNGenerator extends PidGeneratorComponent
    with SeedStorageComponent
    with PidFormatterComponent
    with DatabaseComponent
    with DebugEnhancedLogging {

    // TODO share these instances between DOIGenerator and URNGenerator
    override val database: Database = new Database {}
    override val formatter: PidFormatter = new PidFormatter {}
    override val seedStorage: SeedStorage = new SeedStorage {
      override val pidType: PidType = URN
      override val firstSeed: Long = properties.properties.getLong("pid-generator.types.urn.firstSeed")
    }
    override val generator: PidGenerator = new PidGenerator {
      override val length: Int = 6
      override val illegalChars: Map[Char, Char] = Map.empty
      override val namespace: String = properties.properties.getString("pid-generator.types.urn.namespace")
      override val dashPosition: Int = properties.properties.getInt("pid-generator.types.urn.dashPosition")
    }

    // TODO improve this naked function
    def generate()(implicit connection: Connection): Try[String] = generator.next()
  }
}
