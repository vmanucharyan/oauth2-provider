package oauth2

import java.time.{Duration, LocalDateTime}

class AccessToken(val userId: String,
                  val value: String,
                  val expiresIn: LocalDateTime,
                  val tokenType: String = "bearer") {
  def this(userId: String, value: String, duration: Duration) =
    this(userId, value, LocalDateTime.now() plus duration)

  def isExpired = LocalDateTime.now().isAfter(expiresIn)
}
