import java.rmi.Remote
import java.rmi.RemoteException


interface BulletinInterface: Remote {

    @Throws(RemoteException::class)
    fun addBulletin(bulletin: Bulletin)

    @Throws(RemoteException::class)
    fun getBulletins(): ArrayList<Bulletin>
}