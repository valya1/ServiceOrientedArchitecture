import java.rmi.server.UnicastRemoteObject

class BulletinInterfaceImpl: BulletinInterface, UnicastRemoteObject() {

    private val bullArray = ArrayList<Bulletin>()

    @Synchronized
    override fun addBulletin(bulletin: Bulletin) {
        bullArray.add(bulletin)
        println("Bulletins size: ${bullArray.size}")
    }

    override fun getBulletins(): ArrayList<Bulletin> = bullArray
}