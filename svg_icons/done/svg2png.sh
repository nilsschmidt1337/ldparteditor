#!/bin/bash
ls *.svg | while read file
        do

           destFile=`echo "png/icon8""_"$file | sed 's/\.svg/\.png/'`
           inkscape -f $file -w 8 -h 8 -e $destFile

           destFile=`echo "png/icon16""_"$file | sed 's/\.svg/\.png/'`
           inkscape -f $file -w 16 -h 16 -e $destFile

           destFile=`echo "png/icon24""_"$file | sed 's/\.svg/\.png/'`
           inkscape -f $file -w 24 -h 24 -e $destFile

           destFile=`echo "png/icon32""_"$file | sed 's/\.svg/\.png/'`
           inkscape -f $file -w 32 -h 32 -e $destFile

           destFile=`echo "png/icon48""_"$file | sed 's/\.svg/\.png/'`
           inkscape -f $file -w 48 -h 48 -e $destFile
   
           destFile=`echo "png/icon64""_"$file | sed 's/\.svg/\.png/'`
           inkscape -f $file -w 64 -h 64 -e $destFile

           destFile=`echo "png/icon72""_"$file | sed 's/\.svg/\.png/'`
           inkscape -f $file -w 72 -h 72 -e $destFile

           destFile=`echo "png/icon96""_"$file | sed 's/\.svg/\.png/'`
           inkscape -f $file -w 96 -h 96 -e $destFile

           destFile=`echo "png/icon128""_"$file | sed 's/\.svg/\.png/'`
           inkscape -f $file -w 128 -h 128 -e $destFile

        done
exit 0
