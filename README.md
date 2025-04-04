# LDPartEditor - The LDraw™ Part Editor
"Create and edit [LDraw™](https://www.ldraw.org) Parts quick and easy."

[![Java CI](https://github.com/nilsschmidt1337/ldparteditor/actions/workflows/ant.yml/badge.svg)](https://github.com/nilsschmidt1337/ldparteditor/actions/workflows/ant.yml)

## How to build LDPartEditor

Install a recent version of [Apache Ant](https://ant.apache.org/) and at least a [Java 21 JDK](https://openjdk.java.net/).

### Linux:
> ant -noinput -buildfile build-linux.xml

### Windows (install the [WiX toolset](https://wixtoolset.org/) first):
> ant -noinput -buildfile build-windows.xml

### Mac OS X:
> ant -noinput -buildfile build-macos.xml

### Mac OS X (ARM):
> ant -noinput -buildfile build-macos-arm.xml

## What is this?

![1](https://user-images.githubusercontent.com/11047164/154556480-3c25947c-9f05-4ed3-b379-83e826945f4f.png)

This repository contains the full sourcecode of "LDPartEditor" (further referred to as LDPE).
LDPE aims to be the platform independent LEGO® CAD tool for [LDraw™](https://www.ldraw.org) parts.
You can find a user manual and wiki [here](https://github.com/nilsschmidt1337/ldparteditor/wiki). The manual does not cover all [official LDraw specifications](https://www.ldraw.org/article/218.html) which were ratified by the LDraw Standards Commitee. If you want to start from the beginning, you should probably read the specifications first and gather some experience from [the community forums](https://forums.ldraw.org/) to get started.
Becoming famliar with "part authoring", the creation of parts, is just like playing an instrument: it is a matter of talent, time and effort.

If you want to contact me, please create an **issue** [here](https://github.com/nilsschmidt1337/ldparteditor/issues). I will try to address your request as fast as possible, but keep in mind, that I am doing it as a hobby.

LDPartEditor was not developed by the LDraw™ organization itself. The development process was planned and excecuted by myself as a hobby. I am now a professional software developer and discovered LDraw.org randomly on the net in the past. I left my Dark Ages in 2009 when I used the LDraw™ System of Tools for the first time. Later on, I missed nearly all the patterned parts from my favourite space theme "UFO" (1997–1998). I decided to built them all alone with a complex toolchain involving a commercial 3D editor from a Gaming-IDE, [MLCAD](http://mlcad.lm-software.com/), [SlicerPro](https://www.philohome.com/isecalc/slicerpro.htm) and the [LDDesignPad](https://lddp.sourceforge.net/).
While I progressed on my apprenticeship as a software developer, I invented the [LD Pattern Creator](https://sourceforge.net/projects/patterncreator/), which is now used by LDraw™ part authors to create virtual representations of patterned LEGO® parts. The development of LD Pattern Creator is still ongoing. In contrast to LDPE, the pattern creator supports only 2D edit and is not 100% compliant to the LDraw™ File Format Specification for Part Files.

The program is licensed under the "MIT License" which gives you the freedom to do almost anything with it.
I wrote not the whole application. A small amount of code was written by other people. These people were so kind to distribute it under loose conditions. I want to speak my best wishes to: *Philippe "Philo" Hurbain*, *Michael Hoffer*, *L. Paul Chew*, *Gary W. Lucas*, *Martin Davis*, *Luca Carettoni*, *Peter Bartfai*, *Travis Cobbs*, *Kai Burjack* and *Matthias Mann*.


![ldraw.org](https://lh4.googleusercontent.com/-gm8UHxogrNY/VSa67u-kLkI/AAAAAAAAAXI/akJ3r2ZvsXg/w468-h60-no/ldrawbanner.gif)

LDraw™ is an open standard for LEGO® CAD.

------------------------
LEGO® is a trademark of the LEGO® Group of companies which does not sponsor, authorize or endorse this application.
LEGO has its own homepage: www.lego.com





