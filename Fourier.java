package nl.jive.utils;

import java.lang.Math;

public class Fourier {
	 public static void fourn(float data[], int nn[], 
									  int ndim, int isign)
    {
		  int idim;
		  int i1,i2,i3,i2rev,i3rev,ip1,ip2,ip3,ifp1,ifp2;
		  int ibit,k1,k2,n,nprev,nrem,ntot;
		  float tempi,tempr;
		  double theta,wi,wpi,wpr,wr,wtemp;
  
		  for (ntot=1,idim=1;idim<=ndim;idim++)
				ntot *= nn[idim-1];
		  nprev=1;
		  for (idim=ndim;idim>=1;idim--) {
				n=nn[idim-1];
				nrem=ntot/(n*nprev);
				ip1=nprev << 1;
				ip2=ip1*n;
				ip3=ip2*nrem;
				i2rev=1;
				for (i2=1;i2<=ip2;i2+=ip1) {
					 if (i2 < i2rev) {
						  for (i1=i2;i1<=i2+ip1-2;i1+=2) {
								for (i3=i1;i3<=ip3;i3+=ip2) {
									 i3rev=i2rev+i3-i2;
									 tempr=data[i3-1]; data[i3-1]=data[i3rev-1]; data[i3rev-1]=tempr;
									 tempr=data[i3]; data[i3]=data[i3rev]; data[i3rev]=tempr;
								}
						  }
					 }
					 ibit=ip2 >> 1;
					 while (ibit >= ip1 && i2rev > ibit) {
						  i2rev -= ibit;
						  ibit >>= 1;
					 }
					 i2rev += ibit;
				}
				ifp1=ip1;
				while (ifp1 < ip2) {
					 ifp2=ifp1 << 1;
					 theta=isign*6.28318530717959/(ifp2/ip1);
					 wtemp=Math.sin(0.5*theta);
					 wpr = -2.0*wtemp*wtemp;
					 wpi=Math.sin(theta);
					 wr=1.0;
					 wi=0.0;
					 for (i3=1;i3<=ifp1;i3+=ip1) {
						  for (i1=i3;i1<=i3+ip1-2;i1+=2) {
								for (i2=i1;i2<=ip3;i2+=ifp2) {
									 k1=i2;
									 k2=k1+ifp1;
									 tempr=(float)wr*data[k2-1]-(float)wi*data[k2];
									 tempi=(float)wr*data[k2]+(float)wi*data[k2-1];
									 data[k2-1]=data[k1-1]-tempr;
									 data[k2]=data[k1]-tempi;
									 data[k1-1] += tempr;
									 data[k1] += tempi;
								}
						  }
						  wr=(wtemp=wr)*wpr-wi*wpi+wr;
						  wi=wi*wpr+wtemp*wpi+wi;
					 }
					 ifp1=ifp2;
				}
				nprev *= n;
		  }
    }
	 /* (C) Copr. 1986-92 Numerical Recipes Software #p21E6W)1.1&iE10(9p#. */
}
/*
 
  void rlft2(float data[][], float speq[], int nn2, int nn3, int isign)
  {
  void fourn(float data[], unsigned long nn[], int ndim, int isign);
  int i1,i2,i3,j1,j2,j3,nn[4],ii3;
  double theta,wi,wpi,wpr,wr,wtemp;
  float c1,c2,h1r,h1i,h2r,h2i;
  
  c1=0.5;
  c2 = -0.5*isign;
  theta=isign*(6.28318530717959/nn3);
  wtemp=Math.sin(0.5*theta);
  wpr = -2.0*wtemp*wtemp;
  wpi=Math.sin(theta);
  nn[2]=nn2;
  nn[3]=nn3 >> 1;
  if (isign == 1) {
  fourn(&data[1][1][1]-1,nn,3,isign);
  for (i1=1;i1<=nn1;i1++)
  for (i2=1,j2=0;i2<=nn2;i2++) {
  speq[i1][++j2]=data[i1][i2][1];
  speq[i1][++j2]=data[i1][i2][2];
  }
  }
  for (i1=1;i1<=nn1;i1++) {
  j1=(i1 != 1 ? nn1-i1+2 : 1);
  wr=1.0;
  wi=0.0;
  for (ii3=1,i3=1;i3<=(nn3>>2)+1;i3++,ii3+=2) {
  for (i2=1;i2<=nn2;i2++) {
  if (i3 == 1) {
  j2=(i2 != 1 ? ((nn2-i2)<<1)+3 : 1);
  h1r=c1*(data[i1][i2][1]+speq[j1][j2]);
  h1i=c1*(data[i1][i2][2]-speq[j1][j2+1]);
  h2i=c2*(data[i1][i2][1]-speq[j1][j2]);
  h2r= -c2*(data[i1][i2][2]+speq[j1][j2+1]);
  data[i1][i2][1]=h1r+h2r;
  data[i1][i2][2]=h1i+h2i;
  speq[j1][j2]=h1r-h2r;
  speq[j1][j2+1]=h2i-h1i;
  } else {
  j2=(i2 != 1 ? nn2-i2+2 : 1);
  j3=nn3+3-(i3<<1);
  h1r=c1*(data[i1][i2][ii3]+data[j1][j2][j3]);
  h1i=c1*(data[i1][i2][ii3+1]-data[j1][j2][j3+1]);
  h2i=c2*(data[i1][i2][ii3]-data[j1][j2][j3]);
  h2r= -c2*(data[i1][i2][ii3+1]+data[j1][j2][j3+1]);
  data[i1][i2][ii3]=h1r+wr*h2r-wi*h2i;
  data[i1][i2][ii3+1]=h1i+wr*h2i+wi*h2r;
  data[j1][j2][j3]=h1r-wr*h2r+wi*h2i;
  data[j1][j2][j3+1]= -h1i+wr*h2i+wi*h2r;
  }
  }
  wr=(wtemp=wr)*wpr-wi*wpi+wr;
  wi=wi*wpr+wtemp*wpi+wi;
  }
  }
  if (isign == -1)
  fourn(&data[1][1][1]-1,nn,3,isign);
  }
*/
