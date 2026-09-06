#Purpose
This application read photo files from source directory and move them to destination directory with subdirectories accordind exif data in image or date of file.
#Logic of processing files
Program process  source directory/directories or file/files and move them to destination and produce report of processing files  
## Logic of scaning source directory 

The program scan source directory and all subdirectories and select all photo files .
The photo files are files with extensions
"jpg".
The raw files are files with extension  "crw","cr2","nef" 
The video files are files with extension "mov", "avi","thm", "mp4","3gp","wmv"
The ignored files "thumbs.db","picasa.ini"
All other files are uknown files.
## Logic of processing files 
### Calculation of destionation directory 
It dependes from switch use dsif date or not 
If Use exif date of image is true then for photo files program tries to extract dsif date else the date of file is used .
For all other  types the date of file is used . 
After retreiving the date , th program  calculate real destination directory for file.

For photo files (except panorama files ) the real destination directory  is 
root folder  (target dir from ui )
subfolders YYYY where YYYY is year of photo 
subfolders YYYY-MM-DD
The program creates required  folders if they are not present 
If file exists in destination folder it is not overwritten

For panaroma files the real destination  is  calculated as for photo , and  the subdirectory panorama_NNNNN of the real destination  is used. NNNNN is the digits extracted from 2 pos to 'a' the first file name of panaroma files 

The panorama files are photo files starting sith "st"

For video files the real destination dir is calucalated as for photo and subdirectory video is used 

For raw files the deatination dir is 
root folder  (target dir from ui )
subfolders YYYY where YYYY is year of photo 
subforder "RAW"

The name of destination file is the same as of the source file for all cases 

### logic of moving files 
if file source file and destination file  exists the program compare contents.
    If  it is the same than sources file is removed  and add to the list fileProcessed
    elseif content  is different the files is add to the filesErrors
else  destination file add to filesNew
After this the new files are moved to destinatin 
the same files  are removed 
the files with different content are left as it is 

## Analyze 

report contains 
 -the list of the files which could be moved  (with directory to which they will be moved )
 -the  list of the files which exists in destination and  are identical and should be  removed 
 - the list of files which exist in deatination but the content is different 
 - the lis of files in which date in exif and date of file is not the same 






  




