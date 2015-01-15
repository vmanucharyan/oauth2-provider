package oauth2

import scala.util.Random

trait AppCredsGenerator {
  def generateId(): String
  def generateSecret(): String
}

class RandomAppCredsGenerator(val idLen: Int = 50, val secretLen: Int = 100) {
  def generateId(): String = Random.alphanumeric take idLen toString()
  def generateKey(): String = Random.alphanumeric take secretLen toString()
}
