package ua.com.ostrog.photo.logic;

public enum FileDestination {
    EXIST, // check-square_blue.png      
    NEW,   //check-square_green.png      
    IGNORED, // frown_png
    ERROR, // close-square_red.png           
    DATE_DIFF // exif date.png
}
