/*
 * Copyright 2011 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.liftweb
package mongodb
package record
package field

import common.{Box, Empty, Full}
import http.SHtml
import util.Helpers._

import java.util.UUID

import org.bson.types.ObjectId
import net.liftweb.record.TypedField
import net.liftweb.record.field._

/*
 * Trait for creating a Field for storing a "foreign key". Caches the
 * item after fetching. Versions are available for ObjectId, UUID, String,
 * Int, and Long.
 *
 * toForm produces a select form element. You just need to supply the
 * options by overriding the options method.
 */
trait MongoRefField[RefType <: MongoRecord[RefType], MyType] extends TypedField[MyType] {

  /** The MongoMetaRecord of the referenced object **/
  def refMeta: MongoMetaRecord[RefType]

  /*
  * get the referenced object
  */
  def obj = synchronized {
    if (!_calcedObj) {
      _calcedObj = true
      this._obj = valueBox.flatMap(v => refMeta.findAny(v))
    }
    _obj
  }

  def cached_? : Boolean = synchronized { _calcedObj }

  def primeObj(obj: Box[RefType]) = synchronized {
    _obj = obj
    _calcedObj = true
  }

  private var _obj: Box[RefType] = Empty
  private var _calcedObj = false

  /** Options for select list **/
  def options: List[(Box[MyType], String)] = Nil

  /** Label for the selection item representing Empty, show when this field is optional. Defaults to the empty string. */
  def emptyOptionLabel: String = ""

  def buildDisplayList: List[(Box[MyType], String)] = {
    if (optional_?) (Empty, emptyOptionLabel)::options else options
  }

  private def elem = SHtml.selectObj[Box[MyType]](
    buildDisplayList,
    Full(valueBox),
    setBox(_)
  ) % ("tabindex" -> tabIndex.toString)

  override def toForm =
    if (options.length > 0)
      uniqueFieldId match {
        case Full(id) => Full(elem % ("id" -> (id + "_field")))
        case _ => Full(elem)
      }
    else
      Empty
}

class ObjectIdRefField[OwnerType <: MongoRecord[OwnerType],RefType <: MongoRecord[RefType]](
  rec: OwnerType, rm: MongoMetaRecord[RefType]
)
  extends ObjectIdField[OwnerType](rec)
  with MongoRefField[RefType, ObjectId]
{
  def refMeta = rm
}

class UUIDRefField[OwnerType <: MongoRecord[OwnerType], RefType <: MongoRecord[RefType]](
  rec: OwnerType, rm: MongoMetaRecord[RefType]
)
  extends UUIDField[OwnerType](rec)
  with MongoRefField[RefType, UUID]
{
  def refMeta = rm
}

class StringRefField[OwnerType <: MongoRecord[OwnerType], RefType <: MongoRecord[RefType]](
  rec: OwnerType, rm: MongoMetaRecord[RefType], maxLen: Int
)
  extends StringField[OwnerType](rec, maxLen)
  with MongoRefField[RefType, String]
{
  def refMeta = rm
}

class IntRefField[OwnerType <: MongoRecord[OwnerType], RefType <: MongoRecord[RefType]](
  rec: OwnerType, rm: MongoMetaRecord[RefType]
)
  extends IntField[OwnerType](rec)
  with MongoRefField[RefType, Int]
{
  def refMeta = rm
}

class LongRefField[OwnerType <: MongoRecord[OwnerType], RefType <: MongoRecord[RefType]](
  rec: OwnerType, rm: MongoMetaRecord[RefType]
)
  extends LongField[OwnerType](rec)
  with MongoRefField[RefType, Long]
{
  def refMeta = rm
}
