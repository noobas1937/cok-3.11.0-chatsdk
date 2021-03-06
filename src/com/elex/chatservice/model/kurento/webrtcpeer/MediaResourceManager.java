/*
 * (C) Copyright 2016 VTT (http://www.vtt.fi)
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
 *
 */

package com.elex.chatservice.model.kurento.webrtcpeer;

import android.util.Log;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaCodecVideoEncoder;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.voiceengine.WebRtcAudioManager;

import com.elex.chatservice.model.kurento.LooperExecutor;
import com.elex.chatservice.model.kurento.WebRtcPeerManager;
import com.elex.chatservice.util.LogUtil;

import java.util.EnumSet;
import java.util.HashMap;

/**
 * The class implements the management of media resources.
 *
 * The implementation is based on PeerConnectionClient.java of package org.appspot.apprtc
 * (please see the copyright notice below)
 */

/*
 * libjingle
 * Copyright 2014 Google Inc.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


final class MediaResourceManager implements NBMWebRTCPeer.Observer {
    private static final String TAG = "MediaResourceManager";
    private static final String DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT = "DtlsSrtpKeyAgreement";
    private static final int HD_VIDEO_WIDTH = 1280;
    private static final int HD_VIDEO_HEIGHT = 720;
    private static final int MAX_VIDEO_WIDTH = 1280;
    private static final int MAX_VIDEO_HEIGHT = 1280;
    private static final int MAX_VIDEO_FPS = 30;
    private static final String MAX_VIDEO_WIDTH_CONSTRAINT = "maxWidth";
    private static final String MIN_VIDEO_WIDTH_CONSTRAINT = "minWidth";
    private static final String MAX_VIDEO_HEIGHT_CONSTRAINT = "maxHeight";
    private static final String MIN_VIDEO_HEIGHT_CONSTRAINT = "minHeight";
    private static final String MAX_VIDEO_FPS_CONSTRAINT = "maxFrameRate";
    private static final String MIN_VIDEO_FPS_CONSTRAINT = "minFrameRate";
    private static final String AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation";
    private static final String AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl";
    private static final String AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter";
    private static final String AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression";
    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
    public static final String AUDIO_TRACK_ID = "ARDAMSa0";

    private LooperExecutor executor;
    private PeerConnectionFactory factory;
    private MediaConstraints pcConstraints;
    private MediaConstraints videoConstraints;
    private MediaConstraints audioConstraints;
    private MediaConstraints sdpMediaConstraints;
    private int numberOfCameras;
    private boolean videoCallEnabled;
    private boolean renderVideo;
    private boolean videoSourceStopped;
    private boolean audioCallEnable;
    private boolean renderAudio;
    private boolean audioSourceStoped;
    private MediaStream localMediaStream;
    private VideoSource videoSource;
    private AudioSource audioSource;
    private VideoTrack localVideoTrack;
    private AudioTrack localAudioTrack;
    
    private HashMap<MediaStream,VideoTrack> remoteVideoTracks;
    private HashMap<VideoRenderer.Callbacks,VideoRenderer> remoteVideoRenderers;
    private HashMap<VideoRenderer,MediaStream> remoteVideoMediaStreams;

    private VideoRenderer.Callbacks localRender;
    private NBMWebRTCPeer.NBMPeerConnectionParameters peerConnectionParameters;
    private VideoCapturerAndroid videoCapturer;
    private NBMMediaConfiguration.NBMCameraPosition currentCameraPosition;

    MediaResourceManager(NBMWebRTCPeer.NBMPeerConnectionParameters peerConnectionParameters,
                                LooperExecutor executor, PeerConnectionFactory factory){
        this.peerConnectionParameters = peerConnectionParameters;
        this.localMediaStream = null;
        this.executor = executor;
        this.factory = factory;
        renderVideo = true;
        if(WebRtcPeerManager.getInstance().audioEnable())
        	renderAudio = true;
        else
        	renderAudio = false;
        remoteVideoTracks = new HashMap<MediaStream, VideoTrack>();
        remoteVideoRenderers = new HashMap<VideoRenderer.Callbacks, VideoRenderer>();
        remoteVideoMediaStreams = new HashMap<VideoRenderer, MediaStream>();
        videoCallEnabled = peerConnectionParameters.videoCallEnabled;
    }

    void createMediaConstraints() {
        // Create peer connection constraints.
        pcConstraints = new MediaConstraints();
        // Enable DTLS for normal calls and disable for loopback calls.
        if (peerConnectionParameters.loopback) {
            pcConstraints.optional.add(new MediaConstraints.KeyValuePair(DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT, "false"));
        } else {
            pcConstraints.optional.add(new MediaConstraints.KeyValuePair(DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT, "true"));
        }

        //pcConstraints.optional.add(new MediaConstraints.KeyValuePair(RTPDATACHANNELS_CONSTRAINT, "true"));
        pcConstraints.optional.add(new MediaConstraints.KeyValuePair("internalSctpDataChannels", "true"));

        // Check if there is a camera on device and disable video call if not.
        numberOfCameras = CameraEnumerationAndroid.getDeviceCount();
        if (numberOfCameras == 0) {
            Log.w(TAG, "No camera on device. Switch to audio only call.");
            videoCallEnabled = false;
        }
        // Create video constraints if video call is enabled.
        if (videoCallEnabled) {
            videoConstraints = new MediaConstraints();
            int videoWidth = peerConnectionParameters.videoWidth;
            int videoHeight = peerConnectionParameters.videoHeight;
            // If VP8 HW video encoder is supported and video resolution is not
            // specified force it to HD.
            if ((videoWidth == 0 || videoHeight == 0) && peerConnectionParameters.videoCodecHwAcceleration && MediaCodecVideoEncoder.isVp8HwSupported()) {
                videoWidth = HD_VIDEO_WIDTH;
                videoHeight = HD_VIDEO_HEIGHT;
            }
            // Add video resolution constraints.
            if (videoWidth > 0 && videoHeight > 0) {
                videoWidth = Math.min(videoWidth, MAX_VIDEO_WIDTH);
                videoHeight = Math.min(videoHeight, MAX_VIDEO_HEIGHT);
                videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair(MIN_VIDEO_WIDTH_CONSTRAINT, Integer.toString(videoWidth)));
                videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair(MAX_VIDEO_WIDTH_CONSTRAINT, Integer.toString(videoWidth)));
                videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair(MIN_VIDEO_HEIGHT_CONSTRAINT, Integer.toString(videoHeight)));
                videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair(MAX_VIDEO_HEIGHT_CONSTRAINT, Integer.toString(videoHeight)));
            }
            // Add fps constraints.
            int videoFps = peerConnectionParameters.videoFps;
            if (videoFps > 0) {
                videoFps = Math.min(videoFps, MAX_VIDEO_FPS);
                videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair(MIN_VIDEO_FPS_CONSTRAINT, Integer.toString(videoFps)));
                videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair(MAX_VIDEO_FPS_CONSTRAINT, Integer.toString(videoFps)));
            }
        }
        // Create audio constraints.
        audioConstraints = new MediaConstraints();
        // added for audio performance measurements
        if (peerConnectionParameters.noAudioProcessing) {
            Log.d(TAG, "Disabling audio processing");
            audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, "false"));
            audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "false"));
            audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "false"));
            audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "false"));
        }
        // Create SDP constraints.
        sdpMediaConstraints = new MediaConstraints();
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        if (videoCallEnabled || peerConnectionParameters.loopback) {
            sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        } else {
            sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"));
        }
        //sdpMediaConstraints.optional.add(new MediaConstraints.KeyValuePair(RTPDATACHANNELS_CONSTRAINT, "true"));
        sdpMediaConstraints.optional.add(new MediaConstraints.KeyValuePair(DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT, "true"));
        sdpMediaConstraints.optional.add(new MediaConstraints.KeyValuePair("internalSctpDataChannels", "true"));
    }

    MediaConstraints getPcConstraints(){
        return pcConstraints;
    }

    MediaConstraints getSdpMediaConstraints(){
        return sdpMediaConstraints;
    }

    MediaStream getLocalMediaStream() {
        return localMediaStream;
    }

    void stopVideoSource() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (videoSource != null && !videoSourceStopped) {
                    Log.d(TAG, "Stop video source.");
                    videoSource.stop();
                    videoSourceStopped = true;
                }
            }
        });
    }

    void startVideoSource() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (videoSource != null && videoSourceStopped) {
                    Log.d(TAG, "Restart video source.");
                    videoSource.restart();
                    videoSourceStopped = false;
                }
            }
        });
    }

    private VideoTrack createCapturerVideoTrack(VideoCapturerAndroid capturer) {
        videoSource = factory.createVideoSource(capturer, videoConstraints);
        localVideoTrack = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        localVideoTrack.setEnabled(renderVideo);
        localVideoTrack.addRenderer(new VideoRenderer(localRender));
        return localVideoTrack;
    }
    
    private AudioTrack createCapturerAudioTrack() {
    	audioSource = factory.createAudioSource(audioConstraints);
        localAudioTrack = factory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        localAudioTrack.setEnabled(renderAudio);
        return localAudioTrack;
    }
    

    private class AttachRendererTask implements Runnable {
        private VideoRenderer.Callbacks remoteRender;
        private MediaStream remoteStream;

        private AttachRendererTask(VideoRenderer.Callbacks remoteRender, MediaStream remoteStream){
            this.remoteRender = remoteRender;
            this.remoteStream = remoteStream;
        }
        public void run() {
            Log.d(TAG, "Attaching VideoRenderer to remote stream (" + remoteStream + ")");

            // Check if the remote stream has a video track
            if (remoteStream.videoTracks.size() == 1) {
                // Get the video track
                VideoTrack remoteVideoTrack = remoteStream.videoTracks.get(0);
                // Set video track enabled if we have enabled video rendering
                remoteVideoTrack.setEnabled(renderVideo);

                VideoRenderer videoRenderer = remoteVideoRenderers.get(remoteRender);
                if (videoRenderer != null) {
                    MediaStream mediaStream = remoteVideoMediaStreams.get(videoRenderer);
                    if (mediaStream != null) {
                        VideoTrack videoTrack = remoteVideoTracks.get(mediaStream);
                        if (videoTrack != null) {
                            videoTrack.removeRenderer(videoRenderer);
                        }
                    }
                }

                VideoRenderer newVideoRenderer = new VideoRenderer(remoteRender);
                remoteVideoTrack.addRenderer(newVideoRenderer);
                remoteVideoRenderers.put(remoteRender, newVideoRenderer);
                remoteVideoMediaStreams.put(newVideoRenderer, remoteStream);
                remoteVideoTracks.put(remoteStream, remoteVideoTrack);
                Log.d(TAG, "Attached.");
            }
        }
    }

    void attachRendererToRemoteStream(VideoRenderer.Callbacks remoteRender, MediaStream remoteStream){
        Log.d(TAG, "Schedule attaching VideoRenderer to remote stream (" + remoteStream + ")");
        executor.execute(new AttachRendererTask(remoteRender, remoteStream));
    }

    void createLocalMediaStream(Object renderEGLContext,final VideoRenderer.Callbacks localRender) {
        if (factory == null) { // || isError) {
            Log.e(TAG, "Peerconnection factory is not created");
            return;
        }
        Log.d(TAG, "RenderEGLContext: " + renderEGLContext);
        this.localRender = localRender;

        Log.d(TAG, "PCConstraints: " + pcConstraints.toString());
        if (videoConstraints != null) {
            Log.d(TAG, "VideoConstraints: " + videoConstraints.toString());
        }
        Log.w(TAG, "PCConstraints: " + pcConstraints.toString());
        if (videoCallEnabled) {
            Log.d(TAG, "EGLContext: " + renderEGLContext);
            factory.setVideoHwAccelerationOptions(renderEGLContext, renderEGLContext);
        }
        // Set default WebRTC tracing and INFO libjingle logging.
        // NOTE: this _must_ happen while |factory| is alive!
        Logging.enableTracing("logcat:", EnumSet.of(Logging.TraceLevel.TRACE_DEFAULT), Logging.Severity.LS_INFO);

        localMediaStream = factory.createLocalMediaStream("ARDAMS");
        if (videoCallEnabled) {
            String cameraDeviceName = CameraEnumerationAndroid.getDeviceName(0);
            currentCameraPosition = NBMMediaConfiguration.NBMCameraPosition.BACK;
            String frontCameraDeviceName = CameraEnumerationAndroid.getNameOfFrontFacingDevice();
            if (numberOfCameras > 1 && frontCameraDeviceName != null) {
                cameraDeviceName = frontCameraDeviceName;
                currentCameraPosition = NBMMediaConfiguration.NBMCameraPosition.FRONT;
            }
            Log.d(TAG, "Opening camera: " + cameraDeviceName);
            videoCapturer = VideoCapturerAndroid.create(cameraDeviceName, null);
            if (videoCapturer == null) {
                Log.d(TAG, "Error while opening camera");
                return;
            }
            localMediaStream.addTrack(createCapturerVideoTrack(videoCapturer));
        }
        localMediaStream.addTrack(createCapturerAudioTrack());

        Log.d(TAG, "Local media stream created.");
    }

    void selectCameraPosition(NBMMediaConfiguration.NBMCameraPosition position){
        if (position != currentCameraPosition) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (!videoCallEnabled || numberOfCameras < 2 || videoCapturer == null) {
                        Log.e(TAG, "Failed to switch camera. Video: " + videoCallEnabled + ". . Number of cameras: " + numberOfCameras);
                        return;  // No video is sent or only one camera is available or error happened.
                    }
                    Log.d(TAG, "Switch camera");
                    videoCapturer.switchCamera(null);
                }
            });
            currentCameraPosition = position; // Let's see if we need to handle this after the switch event.
        }
    }

    void setVideoEnabled(final boolean enable) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                renderVideo = enable;
                if (localVideoTrack != null) {
                    localVideoTrack.setEnabled(renderVideo);
                }
                for (VideoTrack tv : remoteVideoTracks.values()) {
                    tv.setEnabled(renderVideo);
                }
            }
        });
    }
    
    void setAudoEnabled(final boolean enable) {
    	if(renderAudio!=enable)
    	{
    		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "enable",enable);
    		executor.execute(new Runnable() {
                @Override
                public void run() {
                	LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "change audio enable",enable);
                    renderAudio = enable;
                    if (localAudioTrack != null) {
                    	localAudioTrack.setEnabled(renderAudio);
                    }
                }
            });
    	}
    }

    boolean getVideoEnabled(){
        return renderVideo;
    }
    
    boolean getAudioEnable(){
    	return renderAudio;
    }

    boolean hasCameraPosition(NBMMediaConfiguration.NBMCameraPosition position){
        boolean retMe = false;

        String backName = CameraEnumerationAndroid.getNameOfBackFacingDevice();
        String frontName = CameraEnumerationAndroid.getNameOfFrontFacingDevice();

        if (position == NBMMediaConfiguration.NBMCameraPosition.ANY &&
                (backName != null || frontName != null)){
            retMe = true;
        } else if (position == NBMMediaConfiguration.NBMCameraPosition.BACK &&
                backName != null){
            retMe = true;

        } else if (position == NBMMediaConfiguration.NBMCameraPosition.FRONT &&
                frontName != null){
            retMe = true;
        }

        return retMe;
    }

    void close(){
        // Uncomment only if you know what you are doing
    	if(localMediaStream!=null)
    	{
    		localMediaStream.dispose();
    		localMediaStream = null;
    	}
        //videoCapturer.dispose();
        //videoCapturer = null;
    }

    @Override
    public void onInitialize() {}

    @Override
    public void onLocalSdpOfferGenerated(SessionDescription localSdpOffer, NBMPeerConnection connection) {
    }

    @Override
    public void onLocalSdpAnswerGenerated(SessionDescription localSdpAnswer, NBMPeerConnection connection) {
    }

    @Override
    public void onIceCandidate(IceCandidate localIceCandidate, NBMPeerConnection connection) {
    }

    @Override
    public void onIceStatusChanged(PeerConnection.IceConnectionState state, NBMPeerConnection connection) {
    }

    @Override
    public void onRemoteStreamAdded(MediaStream stream, NBMPeerConnection connection) {
    }

    @Override
    public void onRemoteStreamRemoved(MediaStream stream, NBMPeerConnection connection) {
        remoteVideoTracks.remove(stream);
    }

    @Override
    public void onPeerConnectionError(String error) {
    }

    @Override
    public void onDataChannel(DataChannel dataChannel, NBMPeerConnection connection) {

    }

    @Override
    public void onBufferedAmountChange(long l, NBMPeerConnection connection, DataChannel channel) {

    }

    @Override
    public void onStateChange(NBMPeerConnection connection, DataChannel channel) {

    }

    @Override
    public void onMessage(DataChannel.Buffer buffer, NBMPeerConnection connection, DataChannel channel) {

    }
}
