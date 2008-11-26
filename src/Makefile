#
# Title:         Makefile
# Function:      Builds the modules which comprise the Virtual Radio 
#                Interferometer
#
#                Usage: % make
#
# Last Modified: 5-May-1998 (N.McKay)
#

SHELL=/usr/bin/csh

vri: Fourier.class vri.class vriAntenna.class vriArr2UVc.class             \
     vriArrDisp.class vriArrEdit.class vriAuxEdit.class vriAuxiliary.class \
     vriConfig.class vriDisplay.class vriDisplayCtrl.class                 \
     vriGreyDisp.class vriImg2UVp.class vriImgDisp.class vriImgEdit.class  \
     vriLocation.class vriObsEdit.class vriObservatory.class               \
     vriStation.class vriTrack.class vriUVc2UVp.class vriUVcDisp.class     \
     vriUVcEdit.class vriUVpDisp.class vriUVpEdit.class vriUVtrack.class
	mv Fourier.class vri.class vriAntenna.class vriArr2UVc.class \
           vriArrDisp.class vriArrEdit.class vriAuxEdit.class        \
           vriAuxiliary.class vriConfig.class vriDisplay.class       \
           vriDisplayCtrl.class vriGreyDisp.class vriImg2UVp.class   \
           vriImgDisp.class vriImgEdit.class vriLocation.class       \
           vriObsEdit.class vriObservatory.class vriStation.class    \
           vriTrack.class vriUVc2UVp.class vriUVcDisp.class          \
           vriUVcEdit.class vriUVpDisp.class vriUVpEdit.class        \
           vriUVtrack.class ..

Fourier.class vri.class vriAntenna.class vriArr2UVc.class             \
vriArrDisp.class vriArrEdit.class vriAuxEdit.class vriAuxiliary.class \
vriConfig.class vriDisplay.class vriDisplayCtrl.class                 \
vriGreyDisp.class vriImg2UVp.class vriImgDisp.class vriImgEdit.class  \
vriLocation.class vriObsEdit.class vriObservatory.class               \
vriStation.class vriTrack.class vriUVc2UVp.class vriUVcDisp.class     \
vriUVcEdit.class vriUVpDisp.class vriUVpEdit.class vriUVtrack.class: vri.java
	javac vri.java

all: vri
