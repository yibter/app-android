package org.coepi.android.worker.cenfetcher

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result.success
import androidx.work.WorkerParameters
import org.coepi.android.cen.CenReportDao
import org.coepi.android.repo.CoEpiRepo
import org.coepi.android.system.log.LogTag.CEN_MATCHING
import org.coepi.android.system.log.log
import org.koin.core.KoinComponent
import org.koin.core.inject

class ContactsFetchWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val coEpiRepo: CoEpiRepo by inject()

    override suspend fun doWork(): Result {
        log.d("Contacts fetch worker started.", CEN_MATCHING)
        coEpiRepo.requestUpdateReports()
        log.d("Contacts fetch worker finished.", CEN_MATCHING)
        return success()
    }
}
