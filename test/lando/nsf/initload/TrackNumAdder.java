package lando.nsf.initload;

import java.nio.file.Path;
import java.text.DecimalFormat;

public final class TrackNumAdder {

    private final DecimalFormat numFmt = new DecimalFormat("000");
    
    public Path addTrackNum(Path path, int trackNum) {
        String track = numFmt.format(trackNum);
        String fileName = path.getFileName().toString();
        
        int lastDot = fileName.lastIndexOf('.');
        
        if( lastDot == -1 || lastDot == 0) {
            fileName += "." + track;
        } else if( lastDot == fileName.length() - 1) {
            fileName += track;
        } else {
            fileName = fileName.substring(0, lastDot) + "." + track + fileName.substring(lastDot);
        }
        
        return path.getParent().resolve(fileName);
    }
}
