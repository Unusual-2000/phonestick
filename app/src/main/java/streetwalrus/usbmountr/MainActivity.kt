package streetwalrus.usbmountr

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import eu.chainfire.libsuperuser.Shell

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private var mShell: Shell.Interactive? = null
    private var mRootGiven = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO: check for root with a better method than this
        Thread {
            mRootGiven = Shell.SU.available()
            if (mRootGiven) {
                mShell = Shell.Builder().useSU().open()
            } else {
                runOnUiThread {
                    Toast.makeText(this, R.string.host_noroot,
                            Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.licenses -> {
                startActivity(Intent(this, LicenseActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun onServeClicked(@Suppress("UNUSED_PARAMETER") v: View) {
        val prefs = fragmentManager.findFragmentById(R.id.prefs) as HostPreferenceFragment
        val path = prefs.getImagePath()
        val ro = prefs.isReadOnly()
        if (path == "") {
            return
        }

        // TODO: run this in a background thread
        if (mRootGiven) {
            val command = "setprop sys.usb.config mass_storage\n" +
                    "while [ \"\$(getprop sys.usb.state)\" != \"mass_storage\" ]; do sleep 1; done\n" +
                    "echo \"$path\" > /sys/class/android_usb/android0/f_mass_storage/lun/file\n" +
                    "echo \"${if (ro) 1 else 0}\" > /sys/class/android_usb/android0/f_mass_storage/lun/ro\n" +
                    "setprop sys.usb.config mass_storage,adb"

            mShell!!.addCommand(command)
            Toast.makeText(this, R.string.host_success, Toast.LENGTH_SHORT).show()
        }
    }

    fun onDisableClicked(@Suppress("UNUSED_PARAMETER") v: View) {
        if (mRootGiven) {
            mShell!!.addCommand(
                    "echo \"\" > /sys/class/android_usb/android0/f_mass_storage/lun/file")
            Toast.makeText(this, R.string.host_disable_success, Toast.LENGTH_SHORT).show()
        }
    }

    fun onEncoderClicked(@Suppress("UNUSED_PARAMETER") v: View) {
        startActivity(Intent(this, EncoderActivity::class.java))
    }
}
