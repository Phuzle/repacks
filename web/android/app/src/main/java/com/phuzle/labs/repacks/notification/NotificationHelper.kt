package com.phuzle.labs.repacks.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import coil3.BitmapImage
import coil3.ImageLoader
import coil3.asDrawable
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.phuzle.labs.repacks.MainActivity
import com.phuzle.labs.repacks.R
import com.phuzle.labs.repacks.data.local.RepackEntity
import com.phuzle.labs.repacks.data.remote.providers.FeedProvider

object NotificationChannels {
    const val CHANNEL_DROPS_ID = "ch_drops"
}

/** Builds the rich `BigPictureStyle` drop notifications per PRD §5.2. */
class NotificationHelper(private val context: Context) {

    fun ensureChannel() {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(NotificationChannels.CHANNEL_DROPS_ID) != null) return
        val channel = NotificationChannel(
            NotificationChannels.CHANNEL_DROPS_ID,
            context.getString(R.string.notification_channel_drops_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.notification_channel_drops_description)
        }
        manager.createNotificationChannel(channel)
    }

    suspend fun notifyNewRepack(entity: RepackEntity, highPriority: Boolean, silent: Boolean) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val bannerBitmap = entity.bannerUrl?.let { loadBitmap(it) }
        val providerName = FeedProvider.fromId(entity.provider)?.displayName ?: entity.provider

        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("repacks://${entity.provider}/${entity.slug}"),
            context,
            MainActivity::class.java,
        )
        val pendingIntent = PendingIntent.getActivity(
            context,
            entity.id.toInt(),
            deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_DROPS_ID)
            .setSmallIcon(R.drawable.ic_repack_monochrome)
            .setContentTitle(entity.title)
            .setContentText("$providerName • Size: ${entity.repackSize ?: "Unknown"}")
            .setPriority(if (highPriority) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSilent(silent)

        if (bannerBitmap != null) {
            builder
                .setLargeIcon(bannerBitmap)
                .setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bannerBitmap)
                        .setSummaryText(entity.description?.take(120)),
                )
        }

        NotificationManagerCompat.from(context).notify(entity.id.toInt(), builder.build())
    }

    private suspend fun loadBitmap(url: String): Bitmap? {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context).data(url).build()
        val result = loader.execute(request) as? SuccessResult ?: return null
        val image = result.image
        (image as? BitmapImage)?.let { return it.bitmap }
        val drawable = image.asDrawable(context.resources)
        (drawable as? BitmapDrawable)?.bitmap?.let { return it }
        val width = drawable.intrinsicWidth.coerceAtLeast(1)
        val height = drawable.intrinsicHeight.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return bitmap
    }
}
