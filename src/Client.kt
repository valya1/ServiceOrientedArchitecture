import java.rmi.Naming
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern


val COMMAND_CONDITION_DELEGATE: (String?) -> (Boolean) = { str ->
    !str.isNullOrEmpty() && str?.toIntOrNull()?.let { it in 1..2 } ?: false
}

val USER_NAME_CONDITION_DELEGATE: (String?) -> (Boolean) = { str -> !str.isNullOrEmpty() }
val EMAIL_CONDITION_DELEGATE: (String?) -> (Boolean) = { str -> !str.isNullOrEmpty() && isEmailValid(str!!) }
val HEADER_CONDITION_DELEGATE: (String?) -> (Boolean) = { str -> !str.isNullOrEmpty() && str!!.length <= 256 }
val TEXT_CONDITION_DELEGATE: (String?) -> (Boolean) = { str -> !str.isNullOrEmpty() }

fun main(vararg args: String) {

    var case: Int

    var bulletinService: BulletinInterface?
    var serverAddress: String? = null
    lateinit var pollingThread: Thread

    var userName: String? = null
    var email: String? = null

    val receivedBulletins = HashSet<Bulletin>()

    while (true) {

        if(serverAddress == null)
            serverAddress = getField(USER_NAME_CONDITION_DELEGATE, "Введите адрес сервера", "")
        bulletinService = aliveServerConnection(serverAddress)

        pollingThread = object: Thread(){
            override fun run() {
                super.run()
                while (true) {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(2))
                    bulletinService?.getBulletins()
                            ?.apply {
                                removeAll(receivedBulletins)
                                forEach(::printBulletin)
                            }?.run { receivedBulletins.addAll(this) }
                }
            }
        }
        pollingThread.start()


        case = getField(COMMAND_CONDITION_DELEGATE, "Что вы хотите сделать?\n" +
                " 1 Создать новое объявление \n" +
                " 2 Посмотреть объявления с сервера" +
                " 3 Получать новые объявления", "Неизвестная команда, попробуйте еще раз").toInt()

        if(userName == null)
            userName = getField(USER_NAME_CONDITION_DELEGATE, "Введите ваше имя: ", "Имя введено неверно, попробуйте еще раз: ")
        if(email == null)
            email = getField(EMAIL_CONDITION_DELEGATE, "Введите ваш e-mail: ", "Email введен неверно, попробуйте еще раз: ")

        if (case == 1) {
            val bulletin = Bulletin(
                    userName,
                    email,
                    getField(HEADER_CONDITION_DELEGATE, "Введите заголовок объявления: ", "Email введен неверно, попробуйте еще раз: "),
                    getField(TEXT_CONDITION_DELEGATE, "Введите текст объявления: ", "")
            )

            receivedBulletins.add(bulletin)
            try {
                bulletinService.addBulletin(bulletin)
                println("Объявление успешно добавлено на сервер")
            } catch (ex: Exception) {
                println("Произошла ошибка подключения")
                bulletinService = aliveServerConnection(serverAddress)
            }
        }

        if (case == 2) {
            bulletinService.getBulletins().forEach(::printBulletin)
        }
    }
}

fun aliveServerConnection(serverAddress: String): BulletinInterface {
    var result: BulletinInterface? = null
    while(result == null){
        println("Подключение...")
        try {
            result = Naming.lookup("rmi://$serverAddress/$BULLETIN_BOARD") as BulletinInterface
        }
        catch (ex: Exception){ }
        finally {
            Thread.sleep(TimeUnit.SECONDS.toMillis(2))
        }
    }

    println("Подключено!")
    return result
}

fun printBulletin(bulletin: Bulletin){
    println("НОВОЕ ОБЪЯВЛЕНИЕ!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
    val encodedEmail = bulletin.email.apply {
        replaceRange(0,
                (indexOfFirst { c -> c=='@' } - 2).let { if(it > 0) it else 0 },
                "******" //зашифровка емайла)))
        )
    }
    println("---------------${bulletin.header}---------------")
    println("From: ${bulletin.userName}, email: $encodedEmail")
    println(bulletin.text)
}

fun getField(delegate: (String?) -> (Boolean), explanationText: String, errorText: String): String {
    var notValidField = true
    var result: String? = null
    println(explanationText)

    while (notValidField) {
        result = readLine()
        notValidField = delegate.invoke(result).not()
        if (notValidField) println(errorText)
    }
    return result!!
}

fun isEmailValid(email: String): Boolean {
    return Pattern.compile(
            "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]|[\\w-]{2,}))@"
                    + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                    + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                    + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                    + "[0-9]{1,2}|25[0-5]|2[0-4][0-9]))|"
                    + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$"
    ).matcher(email).matches()
}