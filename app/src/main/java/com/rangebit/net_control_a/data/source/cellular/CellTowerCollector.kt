package com.rangebit.net_control_a.data.source.cellular

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.telephony.*
import androidx.core.app.ActivityCompat
import com.rangebit.net_control_a.domain.model.CellTowerData
import timber.log.Timber

object CellTowerCollector {

    interface CellTowerCallback {
        fun onResult(data: List<CellTowerData>)
        fun onError(reason: String)
    }

    /* ===== СБОР ДАННЫХ (без проверки разрешений) ===== */
    fun collect(activity: Activity): List<CellTowerData> {

        val result = mutableListOf<CellTowerData>()

        val tm = activity.getSystemService(Activity.TELEPHONY_SERVICE) as TelephonyManager

        // Проверка разрешения
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return result
        }

        val cellInfos: List<CellInfo> = try {
            tm.allCellInfo
        } catch (e: SecurityException) {
            Timber.tag("CELL").e(e, "Permission revoked at runtime")
            return result
        } ?: return result

        for (cellInfo in cellInfos) {

            val data = CellTowerData().apply {
                isRegistered = cellInfo.isRegistered
            }

            when (cellInfo) {

                is CellInfoLte -> {
                    val identity = cellInfo.cellIdentity
                    val signal = cellInfo.cellSignalStrength

                    data.apply {
                        mnc = identity.mncString
                        cid = identity.ci
                        pci = identity.pci
                        rsrp = signal.rsrp
                        rssi = signal.rssi
                    }

                    result.add(data)
                }

                is CellInfoWcdma -> {
                    val identity = cellInfo.cellIdentity
                    val signal = cellInfo.cellSignalStrength

                    data.apply {
                        mnc = identity.mncString
                        cid = identity.cid
                        pci = -1
                        rsrp = -1
                        rssi = signal.dbm
                    }

                    result.add(data)
                }

                is CellInfoGsm -> {
                    val identity = cellInfo.cellIdentity
                    val signal = cellInfo.cellSignalStrength

                    data.apply {
                        mnc = identity.mncString
                        cid = identity.cid
                        pci = -1
                        rsrp = -1
                        rssi = signal.dbm
                    }

                    result.add(data)
                }
            }
        }

        return result
    }
}