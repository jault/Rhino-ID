package structures;

import java.awt.image.BufferedImage;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.IContainer;

import control.Options;

public class Video {
	private Frame[] frames;
	private Frame currentFrame;
	private int currentFrameIndex;
	private static double SECONDS_BETWEEN_FRAMES;
	
    // The video stream index, used to ensure we display frames from one and
    // only one video stream from the media container.
    private static int mVideoStreamIndex = -1;
    
    // Time of last frame write
    private static long mLastPtsWrite = Global.NO_PTS;
    public static long MICRO_SECONDS_BETWEEN_FRAMES;
	
	public Video(String videoFile) {
		SECONDS_BETWEEN_FRAMES = Options.fps;
		MICRO_SECONDS_BETWEEN_FRAMES = (long)(Global.DEFAULT_PTS_PER_SECOND * SECONDS_BETWEEN_FRAMES);
        IMediaReader mediaReader = ToolFactory.makeReader(videoFile);
        
        // Create a Xuggler container object
        IContainer container = IContainer.make();

        // Open up the container
        if (container.open(videoFile, IContainer.Type.READ, null) < 0) {
                throw new IllegalArgumentException("Could not open file: " + videoFile);
        }

        long duration = container.getDuration();
        int seconds = (int) duration / 1000000;
        int frameNum = (int) Math.ceil(seconds / SECONDS_BETWEEN_FRAMES);
        frames = new Frame[frameNum];

        // stipulate that we want BufferedImages created in BGR 24bit color space
        mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
        
        mediaReader.addListener(new ImageSnapListener());

        // read out the contents of the media file and
        // dispatch events to the attached listener
        currentFrameIndex = 0;
        while (mediaReader.readPacket() == null) ;
		
		currentFrameIndex = -1;
	}
	
	private class ImageSnapListener extends MediaListenerAdapter {
		public void onVideoPicture(IVideoPictureEvent event) {
	        if (event.getStreamIndex() != mVideoStreamIndex) {
	            // if the selected video stream id is not yet set, go ahead an
	            // select this lucky video stream
	            if (mVideoStreamIndex == -1)
	                mVideoStreamIndex = event.getStreamIndex();
	            // no need to show frames from this video stream
	            else
	                return;
	        }
	
	        // if uninitialized, back date mLastPtsWrite to get the very first frame
	        if (mLastPtsWrite == Global.NO_PTS)
	            mLastPtsWrite = event.getTimeStamp() - MICRO_SECONDS_BETWEEN_FRAMES;
	
	        // if it's time to write the next frame
	        if (event.getTimeStamp() - mLastPtsWrite >= 
	                MICRO_SECONDS_BETWEEN_FRAMES) {
	            
	        	if(currentFrameIndex < frames.length) {
	        		frames[currentFrameIndex] = new Frame(event.getImage());
	        	}
	            currentFrameIndex++;
	
	            // update last write time
	            mLastPtsWrite += MICRO_SECONDS_BETWEEN_FRAMES;
	        }
		}
	}
	
	public Frame nextFrame() {
		currentFrameIndex++;
		if(currentFrameIndex < frames.length) {
			currentFrame = frames[currentFrameIndex];
			return currentFrame;
		}
		return null;
	}
}