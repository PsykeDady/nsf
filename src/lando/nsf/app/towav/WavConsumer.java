package lando.nsf.app.towav;

import java.io.OutputStream;

import lando.dsp.SimpleDsp;
import lando.nsf.apu.Divider;
import lando.wav.ShortArray;
import lando.wav.WAVWriter;

/**
 * Outputs a single channel of signed 16-bit PCM samples at 44.1khz
 * by sampling the moving average of the APU output. 
 * The moving average of the APU output is a crude highpass.
 * 
 * Tries to apply similar filtering that the APU mixer circuit + downstream
 * circuitry applies.
 * 
 */
final class WavConsumer implements APUSampleConsumer {
    
    private static final int WAV_SAMPLES_PER_SEC = 44_100;
    
    private final OutputStream bout;
    private final boolean disableBandPass;
    private final Divider divider = new Divider(
            NSFRenderer.SYSTEM_CYCLES_PER_SEC/WAV_SAMPLES_PER_SEC);
    
    private ShortArray shorts = new ShortArray();
    private float[] filter;
    private FilteredSampleBuffer samples;
    private TotalingSampleBuffer movingAverage;
    
    WavConsumer(OutputStream bout, boolean disableBandPass) {
        this.bout = bout;
        this.disableBandPass = disableBandPass;
    }
    
    @Override
    public void init() throws Exception {
        SimpleDsp dsp = new SimpleDsp();
        
        float[] highpass = dsp.createHighPass(WAV_SAMPLES_PER_SEC, 0, 440);
        float[] lowpass  = dsp.createLowPass (WAV_SAMPLES_PER_SEC, 14_000, 26_000);
        
        filter = dsp.convolve(highpass, lowpass);
        samples = new FilteredSampleBuffer(filter);
        movingAverage = new TotalingSampleBuffer(divider.getPeriod());
    }

    @Override
    public void consume(float sample) throws Exception {
        
        movingAverage.add(sample);
        
        if( divider.clock() ) {
            emit(clamped(movingAverage.computeAverage()));
        }
    }
    
    private void emit(float sample) throws Exception {
        //I don't multiply by the full 65536/32767 range to give
        //a bit of head room. Playing back files that went the
        //full range was causes anything else playing on my machine
        //to mute.
        if( ! disableBandPass ) {
            //the DC offset caused by the [0, 1] range is "reset"
            //to the [-1, 1] range by the sinc filters.
            shorts.append( (short)(filtered(sample)*32000) );
        } else {
            //raw APU output is [0, 1]
            shorts.append( (short)(sample*64000 - 32000) );
        }
    }
    
    private float clamped(float sample) {
        if( sample < 0f ) return 0f;
        if( sample > 1f ) return 1f;
        
        return sample;
    }
    
    private float filtered(float sample) {
        samples.add(sample);
        
        return samples.filtered();
    }

    @Override
    public void finish() throws Exception {
        new WAVWriter(bout).write(shorts);
    }
}
