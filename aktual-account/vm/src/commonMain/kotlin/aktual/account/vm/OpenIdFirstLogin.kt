package aktual.account.vm

// The first OpenID sign-in makes that user the server owner, and needs the server password if
// password login is on
enum class OpenIdFirstLogin {
  NotFirst,
  First,
  FirstWithPassword,
}
