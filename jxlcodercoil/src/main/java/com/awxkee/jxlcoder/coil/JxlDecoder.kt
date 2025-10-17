/*
 * MIT License
 *
 * Copyright (c) 2023 Radzivon Bartoshyk
 * jxl-coder [https://github.com/awxkee/jxl-coder]
 *
 * Created by Radzivon Bartoshyk on 18/9/2023
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.awxkee.jxlcoder.coil

import android.graphics.Bitmap
import android.os.Build
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import coil3.request.allowRgb565
import coil3.request.bitmapConfig
import coil3.size.Dimension
import coil3.size.Scale
import coil3.size.Size
import coil3.size.pxOrElse
import com.awxkee.jxlcoder.JxlCoder
import com.awxkee.jxlcoder.JxlResizeFilter
import com.awxkee.jxlcoder.PreferredColorConfig
import com.awxkee.jxlcoder.ScaleMode
import kotlinx.coroutines.runInterruptible
import okio.BufferedSource
import okio.ByteString.Companion.toByteString
import java.nio.ByteBuffer

class JxlDecoder(
    private val source: SourceFetchResult,
    private val options: Options,
    private val scaleFilter: JxlResizeFilter,
    private val exceptionLogger: ((Exception) -> Unit)? = null
) : Decoder {

    fun readAllBytes(source: BufferedSource): ByteBuffer {
        return ByteBufferUtil.fromStream(source.inputStream())
    }

    override suspend fun decode(): DecodeResult? = runInterruptible {
        try {
            // ColorSpace is preferred to be ignored due to lib is trying to handle all color profiles by itself
            val sourceData = readAllBytes(source.source.source())

            var mPreferredColorConfig: PreferredColorConfig = when (options.bitmapConfig) {
                Bitmap.Config.ALPHA_8 -> PreferredColorConfig.RGBA_8888
                Bitmap.Config.RGB_565 -> if (options.allowRgb565) PreferredColorConfig.RGB_565 else PreferredColorConfig.DEFAULT
                Bitmap.Config.ARGB_8888 -> PreferredColorConfig.RGBA_8888
                else -> PreferredColorConfig.DEFAULT
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && options.bitmapConfig == Bitmap.Config.RGBA_F16) {
                mPreferredColorConfig = PreferredColorConfig.RGBA_F16
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && options.bitmapConfig == Bitmap.Config.HARDWARE) {
                mPreferredColorConfig = PreferredColorConfig.HARDWARE
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && options.bitmapConfig == Bitmap.Config.RGBA_1010102) {
                mPreferredColorConfig = PreferredColorConfig.RGBA_1010102
            }

            if (options.size == Size.ORIGINAL || (options.size.width is Dimension.Undefined && options.size.height is Dimension.Undefined)) {
                val originalImage = JxlCoder.decodeSampled(
                    byteArray = sourceData,
                    width = -1,
                    height = -1,
                    preferredColorConfig = mPreferredColorConfig
                )

                return@runInterruptible DecodeResult(
                    image = originalImage.asImage(),
                    isSampled = false
                )
            }

            val dstWidth = options.size.width.pxOrElse { 0 }
            val dstHeight = options.size.height.pxOrElse { 0 }
            val scaleMode = when (options.scale) {
                Scale.FILL -> ScaleMode.FILL
                Scale.FIT -> ScaleMode.FIT
            }

            val originalImage = JxlCoder.decodeSampled(
                byteArray = sourceData,
                width = dstWidth,
                height = dstHeight,
                preferredColorConfig = mPreferredColorConfig,
                scaleMode = scaleMode,
                jxlResizeFilter = scaleFilter,
            )

            return@runInterruptible DecodeResult(
                image = originalImage.asImage(),
                isSampled = true
            )
        } catch (e: Exception) {
            exceptionLogger?.invoke(e)
            return@runInterruptible null
        }
    }

    class Factory(
        private val scaleFilter: JxlResizeFilter = JxlResizeFilter.BILINEAR,
        private val exceptionLogger: ((Exception) -> Unit)? = null
    ) : Decoder.Factory {
        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader
        ) = if (isJXL(result.source.source())) {
            JxlDecoder(
                source = result,
                options = options,
                scaleFilter = scaleFilter,
                exceptionLogger = exceptionLogger
            )
        } else null

        companion object {
            private val MAGIC_1 = byteArrayOf(0xFF.toByte(), 0x0A).toByteString()
            private val MAGIC_2 = byteArrayOf(
                0x0.toByte(),
                0x0.toByte(),
                0x0.toByte(),
                0x0C.toByte(),
                0x4A,
                0x58,
                0x4C,
                0x20,
                0x0D,
                0x0A,
                0x87.toByte(),
                0x0A
            ).toByteString()

            private fun isJXL(source: BufferedSource): Boolean {
                return source.rangeEquals(0, MAGIC_1) || source.rangeEquals(
                    0,
                    MAGIC_2
                )
            }
        }
    }

}
