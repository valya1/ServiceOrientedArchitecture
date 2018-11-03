import java.rmi.registry.LocateRegistry

fun main(vararg args: String) {
    try {
        val registry = LocateRegistry.createRegistry(5099)
        registry.rebind(BULLETIN_BOARD, BulletinInterfaceImpl())
        System.err.println("Server ready")
    } catch (ex: Exception) {
        System.err.println("Server exception: " + ex.toString())
        ex.printStackTrace()
    }
}


