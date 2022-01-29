#!/bin/bash
ls *.svg | while read file
        do

           destFile=`echo "png/icon8""_"$file | sed 's/\.svg/\.png/'`
           inkscape -f $file -w 8 -h 8 -e $destFile

           destFile=`echo "png/icon12""_"$file | sed 's/\.svg/\.png/'`
           inkscape -f $file -w 12 -h 12 -e $destFile

           destFile=`echo "png/icon16""_"$file | sed 's/\.svg/\.png/'`
           inkscape -f $file -w 16 -h 16 -e $destFile

           destFile=`echo "png/icon20""_"$file | sed 's/\.svg/\.png/'`
           inkscape -f $file -w 20 -h 20 -e $destFile

           destFile=`echo "png/icon24""_"$file | sed 's/\.svg/\.png/'`
           inkscape -f $file -w 24 -h 24 -e $destFile
   
           destFile=`echo "png/icon28""_"$file | sed 's/\.svg/\.png/'`
           inkscape -f $file -w 28 -h 28 -e $destFile

           destFile=`echo "png/icon32""_"$file | sed 's/\.svg/\.png/'`
           inkscape -f $file -w 32 -h 32 -e $destFile

           destFile=`echo "png/icon36""_"$file | sed 's/\.svg/\.png/'`
           inkscape -f $file -w 36 -h 36 -e $destFile

           destFile=`echo "png/icon40""_"$file | sed 's/\.svg/\.png/'`
           inkscape -f $file -w 40 -h 40 -e $destFile

        done
exit 0
