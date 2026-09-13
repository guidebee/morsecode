/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
//--------------------------------- PACKAGE ------------------------------------
package com.guidebee.game.engine.platform.surfaceview;

//--------------------------------- IMPORTS ------------------------------------
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.EGLConfigChooser;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

//[------------------------------ MAIN CLASS ----------------------------------]
/**
 * {@link EGLConfigChooser} implementation for GLES 1.x and 2.0. Let's hope this
 * really works for all devices. Includes MSAA/CSAA
 * config selection if requested. Taken from GLSurfaceView20, heavily modified
 * to accommodate MSAA/CSAA.
 *
 * @author mzechner
 */
public class EglConfigChooser implements GLSurfaceView.EGLConfigChooser {
    private static final int EGL_OPENGL_ES2_BIT = 4;
    // Requiring both bits (not either) is intentional: eglChooseConfig's
    // EGL_RENDERABLE_TYPE filter is an "all requested bits present" match,
    // and we need a config usable by whichever context version
    // GLSurfaceView20.ContextFactory actually manages to create (ES3, or
    // its ES2 fallback) - see docs/GAMEENGINE_UPGRADE_PLAN.md Phase 3.2.
    // Every ES3-capable Android GPU driver in practice also advertises the
    // ES2 bit on its ES3 configs, so this doesn't exclude any real device.
    private static final int EGL_OPENGL_ES3_BIT_KHR = 0x0040;
    public static final int EGL_COVERAGE_BUFFERS_NV = 0x30E0;
    public static final int EGL_COVERAGE_SAMPLES_NV = 0x30E1;

    protected int mRedSize;
    protected int mGreenSize;
    protected int mBlueSize;
    protected int mAlphaSize;
    protected int mDepthSize;
    protected int mStencilSize;
    protected int mNumSamples;
    protected final int[] mConfigAttribs;
    private int[] mValue = new int[1];

    public EglConfigChooser(int r, int g, int b, int a, int depth, int stencil,
                            int numSamples) {
        mRedSize = r;
        mGreenSize = g;
        mBlueSize = b;
        mAlphaSize = a;
        mDepthSize = depth;
        mStencilSize = stencil;
        mNumSamples = numSamples;

        mConfigAttribs = new int[]{EGL10.EGL_RED_SIZE, 4, EGL10.EGL_GREEN_SIZE, 4,
                EGL10.EGL_BLUE_SIZE, 4,
                EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT | EGL_OPENGL_ES3_BIT_KHR,
                EGL10.EGL_NONE};
    }

    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
        // get (almost) all configs available by using r=g=b=4 so we
        // can chose with big confidence :)
        int[] num_config = new int[1];
        egl.eglChooseConfig(display, mConfigAttribs, null, 0, num_config);
        int numConfigs = num_config[0];

        if (numConfigs <= 0) {
            throw new IllegalArgumentException("No configs match configSpec");
        }

        // now actually read the configurations.
        EGLConfig[] configs = new EGLConfig[numConfigs];
        egl.eglChooseConfig(display, mConfigAttribs, configs, numConfigs, num_config);

        // chose the best one, taking into account multi sampling.
        EGLConfig config = chooseConfig(egl, display, configs);

        return config;
    }

    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) {
        EGLConfig best = null;
        EGLConfig bestAA = null;
        EGLConfig safe = null; // default back to 565 when no exact match found

        for (EGLConfig config : configs) {
            int d = findConfigAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0);
            int s = findConfigAttrib(egl, display, config, EGL10.EGL_STENCIL_SIZE, 0);

            // We need at least mDepthSize and mStencilSize bits
            if (d < mDepthSize || s < mStencilSize) continue;

            // We want an *exact* match for red/green/blue/alpha
            int r = findConfigAttrib(egl, display, config, EGL10.EGL_RED_SIZE, 0);
            int g = findConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0);
            int b = findConfigAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0);
            int a = findConfigAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0);

            // Match RGB565 as a fallback
            if (safe == null && r == 5 && g == 6 && b == 5 && a == 0) {
                safe = config;
            }
            // if we have a match, we chose this as our non AA fallback if that one
            // isn't set already.
            if (best == null && r == mRedSize && g == mGreenSize
                    && b == mBlueSize && a == mAlphaSize) {
                best = config;

                // if no AA is requested we can bail out here.
                if (mNumSamples == 0) {
                    break;
                }
            }

            // now check for MSAA support
            int hasSampleBuffers = findConfigAttrib(egl, display, config,
                    EGL10.EGL_SAMPLE_BUFFERS, 0);
            int numSamples = findConfigAttrib(egl, display, config, EGL10.EGL_SAMPLES, 0);

            // We take the first sort of matching config, thank you.
            if (bestAA == null && hasSampleBuffers == 1
                    && numSamples >= mNumSamples && r == mRedSize && g == mGreenSize
                    && b == mBlueSize && a == mAlphaSize) {
                bestAA = config;
                continue;
            }

            // for this to work we need to call the extension glCoverageMaskNV which is not
            // exposed in the Android bindings. We'd have to link agains the NVidia SDK and
            // that is simply not going to happen.
// // still no luck, let's try CSAA support
            hasSampleBuffers = findConfigAttrib(egl, display, config, EGL_COVERAGE_BUFFERS_NV, 0);
            numSamples = findConfigAttrib(egl, display, config, EGL_COVERAGE_SAMPLES_NV, 0);

            // We take the first sort of matching config, thank you.
            if (bestAA == null && hasSampleBuffers == 1 && numSamples >= mNumSamples
                    && r == mRedSize && g == mGreenSize
                    && b == mBlueSize && a == mAlphaSize) {
                bestAA = config;
                continue;
            }
        }

        if (bestAA != null)
            return bestAA;
        else if (best != null)
            return best;
        else
            return safe;
    }

    private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config,
                                 int attribute, int defaultValue) {
        if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
            return mValue[0];
        }
        return defaultValue;
    }

}
