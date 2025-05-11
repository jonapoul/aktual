package actual.api.client

import org.intellij.lang.annotations.Language

@Language("JSON")
internal val SYNC_LIST_BUDGETS_SUCCESS = """
{
    "status": "ok",
    "data": [
        {
            "deleted": 0,
            "fileId": "b328186c-c919-4333-959b-04e676c1ee46",
            "groupId": "afb25fc0-a294-4f71-ae8f-ce1e3a8fec10",
            "name": "Main Budget",
            "encryptKeyId": "2a66f4de-c530-4c06-8103-a48e26a0ce44",
            "owner": null,
            "usersWithAccess": []
        },
        {
            "deleted": 0,
            "fileId": "cf2b43ee-8067-48ed-ab5b-4e4e5531056e",
            "groupId": "ee90358a-f73e-4aa5-a922-653190fd31b7",
            "name": "Test Budget",
            "encryptKeyId": null,
            "owner": "354d0cfe-cd36-44eb-a404-5990a3ab5c39",
            "usersWithAccess": [
                {
                    "userId": "354d0cfe-cd36-44eb-a404-5990a3ab5c39",
                    "displayName": "",
                    "userName": "",
                    "owner": true
                }
            ]
        }
    ]
}
""".trimIndent()

@Language("JSON")
internal val SYNC_LIST_BUDGETS_FAILURE = """
{
  "status": "error",
  "reason": "Something broke"
}
""".trimIndent()

@Language("JSON")
internal val SYNC_GET_USER_KEY_SUCCESS = """
{
  "status": "ok",
  "data": {
    "id": "2a66f4de-c530-4c06-8103-a48e26a0ce44",
    "salt": "PpZ/z6DD6xtjF89wxZOszZ6CkKXNDoBXdtBlIztmneE=",
    "test": "{\"value\":\"nrhpJgUnl8lZvWxSRMIT0aTRKCOHeddlIuGPfNw0NQR/d81m/ZYRqaOjMwoQHpduSzuAivfVZZEslZihl8WhOs7GVkdghwCjqr083G0261M464wHvQl2v5sB+l8f0/mQE2fco7zUagbA7Q==\",\"meta\":{\"keyId\":\"2a66f4de-c530-4c06-8103-a48e26a0ce44\",\"algorithm\":\"aes-256-gcm\",\"iv\":\"whBQQkPM88iFsLVk\",\"authTag\":\"X/JX1lWCchd0Ekjthlxuzg==\"}}"
  }
}
""".trimIndent()
