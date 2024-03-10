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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import coil.ImageLoader
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.fetch.SourceResult
import coil.request.Options
import coil.size.Scale
import coil.size.Size
import coil.size.pxOrElse
import com.awxkee.jxlcoder.JxlAnimatedImage
import com.awxkee.jxlcoder.JxlResizeFilter
import com.awxkee.jxlcoder.PreferredColorConfig
import com.awxkee.jxlcoder.ScaleMode
import com.awxkee.jxlcoder.animation.AnimatedDrawable
import com.awxkee.jxlcoder.animation.JxlAnimatedStore
import kotlinx.coroutines.runInterruptible
import okio.BufferedSource
import okio.ByteString.Companion.toByteString

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import coil.ImageLoader
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.fetch.SourceResult
import coil.request.Options
import coil.size.Scale
import coil.size.Size
import coil.size.pxOrElse
import com.awxkee.jxlcoder.JxlAnimatedImage
import com.awxkee.jxlcoder.JxlResizeFilter
import com.awxkee.jxlcoder.PreferredColorConfig
import com.awxkee.jxlcoder.ScaleMode
import com.awxkee.jxlcoder.animation.AnimatedDrawable
import com.awxkee.jxlcoder.animation.JxlAnimatedStore
import kotlinx.coroutines.runInterruptible
import okio.BufferedSource
import okio.ByteString.Companion.toByteString

public class AnimatedJxlDecoder(
    private val source: SourceResult,
    private val options: Options,
    private val context: Context,
    private val preheatFrames: Int
) : Decoder {

    override suspend fun decode(): DecodeResult = runInterruptible {
        // ColorSpace is preferred to be ignored due to lib is trying to handle all color profile by itself
        val sourceData = source.source.source().readByteArray()

        var mPreferredColorConfig: PreferredColorConfig = when (options.config) {
            Bitmap.Config.ALPHA_8 -> PreferredColorConfig.RGBA_8888
            Bitmap.Config.RGB_565 -> if (options.allowRgb565) PreferredColorConfig.RGB_565 else PreferredColorConfig.DEFAULT
            Bitmap.Config.ARGB_8888 -> PreferredColorConfig.RGBA_8888
            else -> PreferredColorConfig.DEFAULT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && options.config == Bitmap.Config.RGBA_F16) {
            mPreferredColorConfig = PreferredColorConfig.RGBA_F16
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && options.config == Bitmap.Config.HARDWARE) {
            mPreferredColorConfig = PreferredColorConfig.HARDWARE
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && options.config == Bitmap.Config.RGBA_1010102) {
            mPreferredColorConfig = PreferredColorConfig.RGBA_1010102
        }

        if (options.size == Size.ORIGINAL) {
            val originalImage = JxlAnimatedImage(
                byteArray = sourceData,
                preferredColorConfig = mPreferredColorConfig
            )
            return@runInterruptible DecodeResult(
                drawable = originalImage.animatedDrawable(),
                isSampled = false
            )
        }

        val dstWidth = options.size.width.pxOrElse { 0 }
        val dstHeight = options.size.height.pxOrElse { 0 }
        val scaleMode = when (options.scale) {
            Scale.FILL -> ScaleMode.FILL
            Scale.FIT -> ScaleMode.FIT
        }

        val originalImage = JxlAnimatedImage(
            byteArray = sourceData,
            preferredColorConfig = mPreferredColorConfig,
            scaleMode = scaleMode,
            jxlResizeFilter = JxlResizeFilter.BILINEAR
        )
        return@runInterruptible DecodeResult(
            drawable = originalImage.animatedDrawable(
                dstWidth = dstWidth,
                dstHeight = dstHeight
            ),
            isSampled = true
        )
    }

    private fun JxlAnimatedImage.animatedDrawable(
        dstWidth: Int = 0,
        dstHeight: Int = 0
    ): Drawable = AnimatedDrawable(
        frameStore = JxlAnimatedStore(
            jxlAnimatedImage = this,
            targetWidth = dstWidth,
            targetHeight = dstHeight
        ),
        preheatFrames = preheatFrames,
        firstFrameAsPlaceholder = true
    )

    class Factory(
        private val context: Context,
        private val preheatFrames: Int = 6
    ) : Decoder.Factory {
        override fun create(
            result: SourceResult,
            options: Options,
            imageLoader: ImageLoader
        ) = if (isJXL(result.source.source())) {
            AnimatedJxlDecoder(
                source = result,
                options = options,
                context = context,
                preheatFrames = preheatFrames
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
