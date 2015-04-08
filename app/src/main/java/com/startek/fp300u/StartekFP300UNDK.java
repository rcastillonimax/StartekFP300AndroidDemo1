package com.startek.fp300u;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.hardware.usb.*;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.util.Log;
import android.app.PendingIntent;

public class StartekFP300UNDK extends Activity {
	
	final int    U_LEFT  =  -41;
	final int    U_RIGHT =  -42;
	final int    U_UP    =  -43;
	final int    U_DOWN  =  -44;
	final int    U_POSITION_CHECK_MASK   =   0x00002F00;
	final int    U_POSITION_NO_FP        =   0x00002000;
	final int    U_POSITION_TOO_LOW      =   0x00000100;
	final int    U_POSITION_TOO_TOP      =   0x00000200;
	final int    U_POSITION_TOO_RIGHT    =   0x00000400;
	final int    U_POSITION_TOO_LEFT     =   0x00000800;
	final int    U_POSITION_TOO_LOW_RIGHT  = (U_POSITION_TOO_LOW|U_POSITION_TOO_RIGHT);
	final int    U_POSITION_TOO_LOW_LEFT   = (U_POSITION_TOO_LOW|U_POSITION_TOO_LEFT);
	final int    U_POSITION_TOO_TOP_RIGHT  = (U_POSITION_TOO_TOP|U_POSITION_TOO_RIGHT);
	final int    U_POSITION_TOO_TOP_LEFT   = (U_POSITION_TOO_TOP|U_POSITION_TOO_LEFT);
	
	final int    U_POSITION_OK         =    0x00000000;
	
	final int    U_DENSITY_CHECK_MASK   =    0x000000E0;
	final int    U_DENSITY_TOO_DARK     =    0x00000020;
	final int    U_DENSITY_TOO_LIGHT    =    0x00000040;
	final int    U_DENSITY_LITTLE_LIGHT  =   0x00000060;
	final int    U_DENSITY_AMBIGUOUS     =   0x00000080;

	final int    U_INSUFFICIENT_FP     = -31;
	final int    U_NOT_YET             = -32;
	            	            
	final int U_CLASS_A = 65;
	final int U_CLASS_B = 66;
	final int U_CLASS_C = 67;
	final int U_CLASS_D = 68;
	final int U_CLASS_E = 69;
	final int U_CLASS_R = 82;
	
    /** Called when the activity is first created. */
    private TextView theMessage;
    private Button buttonConnect;
    private Button buttonCapture;
    private Button buttonEnroll;
    private Button buttonVerify;
    private Button buttonShow;
    private Button buttonDisC;
    private int connectrtn;
    private int rtn;
    private int rtn2;
    private ImageView myImage; 
    
    //byte[] bMapArray= new byte[1078+(256*324)];
    byte[] bMapArray= new byte[1078+(640*480)];
    private byte[] minu_code1 = new byte[512];
    private byte[] minu_code2 = new byte[512];
    
    private EventHandler m_eventHandler;
    private Bitmap bMap;
    
    private int counter = 0;
    private static Context Context;
    
    //public static final int UPDATE_TEXT_VIEW=0x0001;
////////holing add for usb host
    private static final String ACTION_USB_PERMISSION =
    	    "com.android.example.USB_PERMISSION";
	 private UsbManager manager;
	 private PendingIntent mPermissionIntent;
	 private UsbDevice d ;
	 private UsbDeviceConnection conn;
	 private UsbInterface usbIf;
		UsbEndpoint epIN  ;
		UsbEndpoint epOUT ;
		UsbEndpoint ep2IN ;
		
    	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

    	    public void onReceive(Context context, Intent intent) {
    	        String action = intent.getAction();
    	        if (ACTION_USB_PERMISSION.equals(action)) {
    	            synchronized (this) {
    	                UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

    	                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
    	                    if(device != null){
    	                      //call method to set up device communication
    	                    	theMessage.setText(theMessage.getText()+"\nfp300 found and try connect");
    	                    	connectreader();
    	                   }
    	                } 
    	                else {
    	              //      Log.d(TAG, "permission denied for device " + device);
    	                	theMessage.setText(theMessage.getText()+"\nfp300 found");
    	                }
    	            }
    	        }
    	    }
    	};

    	
    	private void connectreader() {
			// TODO Auto-generated method stub
			
			  usbIf = d.getInterface(0);
			     Log.d("FP300","Interface:-"+ String.valueOf(usbIf.getEndpointCount()));
			    Log.d("FP300" , "Interface Count: "+ Integer.toString(d.getInterfaceCount()));
			    
			    
			    Log.d("USB", String.valueOf(usbIf.getEndpointCount()));
			    
			 //    final UsbEndpoint  usbEndpoint = usbInterface.getEndpoint(0);
			    
				 epIN = null;
				 epOUT = null;
				 ep2IN = null;

		
				 theMessage.setText(theMessage.getText()+"\nnum of ep"+usbIf.getEndpointCount());
				 
				 epOUT = usbIf.getEndpoint(0);	
				 epIN = usbIf.getEndpoint(1);
				 ep2IN = usbIf.getEndpoint(2);
				 
			//	 theMessage.setText(theMessage.getText()+"\nep num "+ ep2IN.getEndpointNumber()+"packet size "+ ep2IN.getMaxPacketSize()+"dir "+ep2IN.getDirection());
			//	 theMessage.setText(theMessage.getText()+"\nep num "+ epIN.getEndpointNumber()+"packet size "+ epIN.getMaxPacketSize()+"dir "+epIN.getDirection());
				 theMessage.setText(theMessage.getText()+"\nep num "+ epOUT.getEndpointNumber()+"packet size "+ epOUT.getMaxPacketSize()+"dir "+epOUT.getDirection());
			   
			//	 theMessage.setText(theMessage.getText()+"\nmanager.hasPermission()");
			     if (!manager.hasPermission(d))
			     {
			    	 theMessage.setText(theMessage.getText()+"\nmanager.hasPermission() false");
			    	 return ; 
			    	 
			     }
			    
			    
			    
				conn =  manager.openDevice(d);
				
				 if(conn.getFileDescriptor() == -1)
			        {
			             Log.d("FM220", "Fails to open DeviceConnection");
			        }
				 else
				 {
					 
					  Log.d("FM220", "Opened DeviceConnection" + Integer.toString(conn.getFileDescriptor())); 
				 }
				 
				 
				 if(conn.releaseInterface(usbIf))
			        {
			             Log.d("USB", "Released OK");
			        }
			        else
			        {
			            Log.d("USB", "Released fails");
			        }


			      if(conn.claimInterface(usbIf, true))
			      {
			          Log.d("USB", "Claim OK");
			      }
			     else
			     {
			         Log.d("USB", "Claim fails");
			     }
			 //     theMessage.setText(theMessage.getText()+"\nEEPROM_read");
			 //     byte [] buf= new byte [48];
			 //     eeprom_read(0,48,buf);
			      theMessage.setText(theMessage.getText()+"\nfm220 fileDesc" + conn.getFileDescriptor());
	

			      
    	}
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main); 
       
        
        InitialSDK();  
                       
        theMessage = (TextView)findViewById(R.id.message);      
        
        theMessage.setText(theMessage.getText()+"\nSTARTEK FP300 Android SDK 0.16 holing build 2014105281743");
        Context=getApplicationContext();
        buttonConnect = (Button)findViewById(R.id.connectB);
  		buttonCapture = (Button)findViewById(R.id.captureB);
		buttonEnroll = (Button)findViewById(R.id.enrollB);
		buttonVerify = (Button)findViewById(R.id.verifyB);
		buttonShow = (Button)findViewById(R.id.showB);
		buttonDisC = (Button)findViewById(R.id.discB);
		myImage = (ImageView)findViewById(R.id.test_image); 
		
		
		
		
		//holing reserve for android.hardware.usb test
		//UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
		manager = (UsbManager) getSystemService(Context.USB_SERVICE);

		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		registerReceiver(mUsbReceiver, filter);
		
		//theMessage.setText(theMessage.getText()+"\nSTARTEK FM210 UsbManager manager test");
		// check for existing devices

		//PendingIntent mPermissionIntent;
        for ( UsbDevice mdevice :  manager.getDeviceList().values()) {

        	int pid,vid;
        	
        	pid=mdevice.getProductId();
        	vid=mdevice.getVendorId();
  
        	if(((pid==0x8360)&&(vid==0x0bca))||((pid==0x8160)&&(vid==0x0bca))){
        		theMessage.setText(theMessage.getText()+"\nfm220 pid found");
        		d=mdevice;
        		
        		manager.requestPermission(d, mPermissionIntent);
        		
        		
        		break;
        		
        	}
        		
        }         
        /////ori connect here



        //Scroll for textview
        theMessage.setMovementMethod(new ScrollingMovementMethod());
      //Connect
      		buttonConnect.setOnClickListener(new Button.OnClickListener(){
      			@Override
      		
      			public void onClick(View v){ 	
      				
      			    //Log.v("Fm210", "Marcus: Click");
      				try{
      		
      					if(conn.getFileDescriptor() == -1)
      			        {
      						connectreader();
      			        	theMessage.setText(theMessage.getText()+"\ntry connect without file descripter"+ conn.getFileDescriptor());
      			        	connectrtn=FP_ConnectCaptureDriver(conn.getFileDescriptor());
      			             Log.d("FM220", "Fails to open DeviceConnection");
      			        }
      					 else
      					 {
      						 theMessage.setText(theMessage.getText()+"\ntry connect with file descripter"+ conn.getFileDescriptor());
      						 connectrtn=FP_ConnectCaptureDriver(conn.getFileDescriptor());
      						  Log.d("FM220", "Opened DeviceConnection" + Integer.toString(conn.getFileDescriptor())); 
      					 }
      				
      			      				
      			}
      			catch(Exception e){
      				e.printStackTrace();	
      			}
      			
      											
      			}
      		});
        
		//Capture
		buttonCapture.setOnClickListener(new Button.OnClickListener(){
			@Override
		
			public void onClick(View v){ 	
				
			    //Log.v("Fm210", "Marcus: Click");
				try{
				
				if(connectrtn==0){
					m_eventHandler = new EventHandler(Looper.getMainLooper());
//					CaptureThread m_captureThread = new CaptureThread(m_eventHandler);
//					Thread m_capture = new Thread(m_captureThread);
//					m_capture.start();
					buttonCapture.setEnabled(false);
					
					new Thread(){
						public void run(){
							super.run();  		

							FP_Capture();
							FP_Capture();
							Message msg0 = new Message();
							msg0.what = PublicData.TEXTVIEW_CAPTURE_PLEASE_PRESS;
							m_eventHandler.sendMessage(msg0);
								
							counter++;
							if((counter%15)==0){
								Log.v("Fm210", "Start GC");
								System.gc();
							}
							
							
							Log.v("Fm210", "Marcus: run");
							//InitialSDK();
							//Log.v("Fm210", "Marcus: InitialSDK() OK");
							//PublicData.captureDone=false;
							while((rtn=FP_Capture())!=0){
								Message msg2 = new Message();
								msg2.what = PublicData.SHOW_PIC; 
								m_eventHandler.sendMessage(msg2);
								
							}
							Log.v("Fm210", "Marcus: FP_Capture OK");
							//FP_SaveImageBMP("/system/data/fp_image.bmp");
							//FP_SaveImageBMP("/data/data/com.startek.fm210/fp_image.bmp");
							FP_SaveImageBMP(Context.getFilesDir().getPath()+"/fp_image.bmp");
							
							
							
							Message msg1 = new Message();
							msg1.what = PublicData.TEXTVIEW_SUCCESS;
							m_eventHandler.sendMessage(msg1);
							//Log.v("Fm210", "Marcus: FP_SaveImageBMP OK");
//							try{
//							//Thread.sleep(100);							
//							}
//							catch(Exception e){}
							
							Message msg2 = new Message();
							//msg2 = new Message();
							msg2.what = PublicData.SHOW_PIC; 
							m_eventHandler.sendMessage(msg2);
							
						}
					}.start(); 
				}else{
					theMessage.setText(theMessage.getText()+"\nFP_ConnectCaptureDriver() failed!!");
    		    	theMessage.postInvalidate();
				    FP_DisconnectCaptureDriver();
					return;
				}
				
			}
			catch(Exception e){
				e.printStackTrace();	
			}
			
											
			}
		});
	
		//Enroll
		buttonEnroll.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v){				
		//		led_off();
		
				
				
				if(connectrtn==0){
					buttonEnroll.setEnabled(false);	
					m_eventHandler = new EventHandler(Looper.getMainLooper());
					
					//let thread do main job
					new Thread(){
						public void run(){
							super.run();  		
														
						    FP_CreateEnrollHandle();

						    Message msg0 = new Message();
							msg0.what = PublicData.TEXTVIEW_ENROLL_PLEASE_PRESS;
							m_eventHandler.sendMessage(msg0);
				            
							for(int i=0;i<6;i++){
				            	 //theMessage.setText(theMessage.getText()+"\nTimes: "+i);
				            	 SystemClock.sleep(500);
				            	 while((rtn=FP_Capture())!= 0){
				            		 	Message msg1 = new Message();
										msg1.what = PublicData.TEXTVIEW_PRESS_AGAIN;
										m_eventHandler.sendMessage(msg1);
										Message msg2 = new Message();
										msg2.what = PublicData.SHOW_PIC; 
										m_eventHandler.sendMessage(msg2);
				            	 }
					             rtn=FP_GetTemplate(minu_code1);
				
					             //if(rtn==0)
					            	 //theMessage.setText(theMessage.getText()+"\nFP_GetTemplate() OK");

					             rtn=FP_ISOminutiaEnroll(minu_code1, minu_code2);
					             //theMessage.setText(theMessage.getText()+"\nenroll rtn="+rtn);
			        
					             	while(true){
					             		rtn2=FP_CheckBlank();
										
					             		Message msg2 = new Message();
					             		msg2.what = PublicData.TEXTVIEW_REMOVE_FINGER;
										m_eventHandler.sendMessage(msg2);
					             		
										if(rtn2!=-1)
					             			break;
					             		//theMessage.setText(theMessage.getText()+"\nremove your finger!!!");
					             	}
			                           
					             	if(rtn==U_CLASS_A || rtn==U_CLASS_B){
					             		//FP_SaveISOminutia(minu_code2, "/system/data/fpcode.dat");
					             		//FP_SaveISOminutia(minu_code2, "/data/data/com.startek.fm210/fpcode.dat");
					             		FP_SaveISOminutia(minu_code2, Context.getFilesDir().getPath()+"/fpcode.dat");
					             		
					             		SystemClock.sleep(1000);
					             		Message msg3 = new Message();
					             		msg3.what = PublicData.TEXTVIEW_SUCCESS;
										m_eventHandler.sendMessage(msg3);		
										
					             		break;
					             	}else if(i==5){
					             		Message msg4 = new Message();
					             		msg4.what = PublicData.TEXTVIEW_FAILURE;
										m_eventHandler.sendMessage(msg4);
					             	}
					             	//showPic();
				            }

				            FP_DestroyEnrollHandle();		
						}
					}.start(); 
				}else{
					theMessage.setText(theMessage.getText()+"\nFP_ConnectCaptureDriver() failed!!");
				    FP_DisconnectCaptureDriver();
					return;
				}

			
			}
			
		});	
		
		buttonVerify.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v){ 	
				try{
				if(connectrtn==0){
					m_eventHandler = new EventHandler(Looper.getMainLooper());
					buttonVerify.setEnabled(false);
					
					new Thread(){
						public void run(){
							super.run();  		
											
							//if((rtn=FP_LoadISOminutia(minu_code2, "/system/data/fpcode.dat"))==0){
							//if((rtn=FP_LoadISOminutia(minu_code2, "/data/data/com.startek.fm210/fpcode.dat"))==0){
							if((rtn=FP_LoadISOminutia(minu_code2, Context.getFilesDir().getPath()+"/fpcode.dat"))==0){						
								Message msg1 = new Message();
								msg1.what = PublicData.TEXTVIEW_FILE_EXIST;
								m_eventHandler.sendMessage(msg1);
								if(connectrtn==0){}
								else{
									FP_DisconnectCaptureDriver();
									return;
								}
														
								counter++;
								if((counter%15)==0){
									Log.v("Fm210", "Start GC");
									System.gc();
								}
								
								try{
									Thread.sleep(1000);							
								}
								catch(Exception e){}
								Message msg0 = new Message();
								msg0.what = PublicData.TEXTVIEW_VERIFY_PLEASE_PRESS;
								m_eventHandler.sendMessage(msg0);
							
								while((rtn=FP_Capture())!=0){
									Message msg2 = new Message();
									msg2.what = PublicData.SHOW_PIC; 
									m_eventHandler.sendMessage(msg2);
								}
							
								//FP_SaveImageBMP("/system/data/fp_image.bmp");
								//FP_SaveImageBMP("/data/data/com.startek.fm210/fp_image.bmp");
								FP_SaveImageBMP(Context.getFilesDir().getPath()+"/fp_image.bmp");
								
								rtn=FP_GetTemplate(minu_code1);
								rtn=FP_ISOminutiaMatchEx(minu_code1, minu_code2);	
								if(rtn>=-1){
									Message msg2 = new Message();
									msg2 = new Message();
									msg2.what = PublicData.TEXTVIEW_SCORE; 
									m_eventHandler.sendMessage(msg2);
									
									Message msg3 = new Message();
									msg3 = new Message();
									msg3.what = PublicData.SHOW_PIC; 
									m_eventHandler.sendMessage(msg3);
								}
					
							}else{
								Message msg4 = new Message();
								msg4.what = PublicData.TEXTVIEW_FILE_NOT_EXIST;
								m_eventHandler.sendMessage(msg4);
								return;	
							}
						}
					}.start(); 
				}else{
					theMessage.setText(theMessage.getText()+"\nFP_ConnectCaptureDriver() failed!!");
    		    	theMessage.postInvalidate();
				    FP_DisconnectCaptureDriver();
					return;
				}
			}
			catch(Exception e){
				e.printStackTrace();	
			}
											
			}
		});

		buttonShow.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v){}
		});		
		
		buttonDisC.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v){
				
				conn.close();
				FP_DisconnectCaptureDriver();
				theMessage.setText(theMessage.getText()+"\nFP_DisconnectCaptureDriver() Succeeded!!");
		    	theMessage.postInvalidate();
		    	
		    	
			}
		});		
		
	}
	
    static {
    	System.loadLibrary("startekfp300u_jni");
    }
    
    private native void InitialSDK();
    private native int FP_ConnectCaptureDriver(int number);
    private native void FP_DisconnectCaptureDriver();
    private native int FP_Capture();
    private native int FP_CheckBlank();
    private native void FP_SaveImageBMP(String filepath);
    private native int FP_CreateEnrollHandle();
    private native int FP_GetTemplate(byte[] m1);
    private native int FP_ISOminutiaEnroll(byte[] m1, byte[] m2);
    private native void FP_SaveISOminutia(byte[] m2, String filepath);
    private native void FP_DestroyEnrollHandle();
    private native int FP_LoadISOminutia(byte[] m2, String filepath); 
    private native int FP_ISOminutiaMatchEx(byte[] m1, byte[] m2);
    private native int Score();
    private native void FP_GetImageBuffer(byte[] bmpBuffer);
    
    class showPic extends AsyncTask<String, Void, String>{
 //       private ImageView image;
        private Bitmap bMap=null;	
    	
        @Override
		protected String doInBackground(String... path) {        	
        	tryGetStream();
        	return null;
		}
		
	    protected void onPostExecute(String a) {
	    	myImage.setImageBitmap(bMap);	    	
	    	bMap = null;
	    	System.gc();
	    	publishProgress();
	    }
	    
	    @Override
		protected void onProgressUpdate(Void... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			myImage.postInvalidate(); 
			//Log.v("Fm210", "Marcus: onProgressUpdate");
		}

		private void tryGetStream(){
	         try {
	             //buf = FP_GetImageBuffer);
	             FP_GetImageBuffer(bMapArray); 
	             	             		    	
	             bMap = BitmapFactory.decodeByteArray(bMapArray, 0, bMapArray.length);
	    	
	         }catch (Exception e) {}
	    }
	    	    
    }
    
    public class EventHandler extends Handler{
		public EventHandler(Looper looper){ 
            super(looper); 
        } 
	
    	public void handleMessage(Message msg){
    		switch(msg.what){
    		    case PublicData.TEXTVIEW_SUCCESS:
    		    	buttonCapture.setEnabled(true);
    		    	buttonEnroll.setEnabled(true);
    		    	buttonVerify.setEnabled(true);
    		    	theMessage.setText(theMessage.getText()+"\nSuccess.");
    		    	theMessage.postInvalidate();
    		    	break;
    		    case PublicData.TEXTVIEW_FAILURE:
    		    	theMessage.setText(theMessage.getText() + "\nFailure.");
    		    	theMessage.postInvalidate();
    		    	buttonEnroll.setEnabled(true);
    		    	break;
    		    case PublicData.TEXTVIEW_CAPTURE_PLEASE_PRESS:
    		    	theMessage.setText(theMessage.getText()+"\nCapture: Press your finger");
    		    	theMessage.postInvalidate();
    		    	break;
    		    case PublicData.TEXTVIEW_ENROLL_PLEASE_PRESS:
    		    	theMessage.setText(theMessage.getText()+"\nEnroll: Press your finger");
    		    	theMessage.postInvalidate();
    		    	break;
    		    case PublicData.TEXTVIEW_VERIFY_PLEASE_PRESS:
    		    	theMessage.setText(theMessage.getText()+"\nVerify: Press your finger");
    		    	theMessage.postInvalidate();
    		    	break;
    		    case PublicData.TEXTVIEW_SCORE:
    		    	if(Score()>500)
    		    		theMessage.setText(theMessage.getText()+"\nMatching Success!! Score="+(int)Score());
    		    	else
    		    		theMessage.setText(theMessage.getText()+"\nMatching Fail!! Score="+(int)Score());
    		    	theMessage.postInvalidate();
    		    	break;
    		    case PublicData.TEXTVIEW_FILE_EXIST:	
    		    	theMessage.setText(theMessage.getText()+"\nVerify: File exist");
    		    	theMessage.postInvalidate();
    		    	break;    
    		    case PublicData.TEXTVIEW_FILE_NOT_EXIST:	
    		    	theMessage.setText(theMessage.getText()+"\nFile not exist, please enroll first");
    		    	theMessage.postInvalidate();
    		    	buttonVerify.setEnabled(true);
    		    	break;    		    		    	
    		    case PublicData.TEXTVIEW_REMOVE_FINGER:
    		    	theMessage.setText(theMessage.getText() + "\nPlease remove your finger");
    		    	theMessage.postInvalidate();
    		    	//new showPic().execute("/system/data/fp_image.bmp");
    		    	new showPic().execute("");
    		    	
    		    	break;
    		    case PublicData.TEXTVIEW_PRESS_AGAIN:
    		    	theMessage.setText(theMessage.getText() + "\nPlease press your finger again");
    		    	theMessage.postInvalidate();
    		    	//new showPic().execute("/system/data/fp_image.bmp");
    		    	new showPic().execute("");
    		    	break;       		    	
    		    case PublicData.SHOW_PIC:	
    		    	//new showPic().execute("/system/data/fp_image.bmp");
    		    	new showPic().execute("");
    		    	buttonCapture.setEnabled(true);
    		    	buttonEnroll.setEnabled(true);
    		    	buttonVerify.setEnabled(true);
    		    	break;
    		    	
    		}  
    		super.handleMessage(msg);
    	}    	
    }
}
