package applet;


import javacard.framework.*;




public class TheApplet extends Applet {

	OwnerPIN writepincode;
	OwnerPIN readpincode;
	static final byte UPDATECARDKEY				= (byte)0x14;
	static final byte UNCIPHERFILEBYCARD			= (byte)0x13;
	static final byte CIPHERFILEBYCARD			= (byte)0x12;
	static final byte CIPHERANDUNCIPHERNAMEBYCARD		= (byte)0x11;
	static final byte READFILEFROMCARD			= (byte)0x10;
	static final byte WRITEFILETOCARD			= (byte)0x09;
	static final byte UPDATEWRITEPIN			= (byte)0x08;
	static final byte UPDATEREADPIN				= (byte)0x07;
	static final byte DISPLAYPINSECURITY			= (byte)0x06;
	static final byte DESACTIVATEACTIVATEPINSECURITY	= (byte)0x05;
	static final byte ENTERREADPIN				= (byte)0x04;
	static final byte ENTERWRITEPIN				= (byte)0x03;
	static final byte READNAMEFROMCARD			= (byte)0x02;
	static final byte WRITENAMETOCARD			= (byte)0x01;
	final static short SW_VERIFICATION_FAILED = 0x6300;
	final static short SW_PIN_VERIFICATION_REQUIRED = 0x6301;

	final static short NVRSIZE      = (short)1024;
	static byte[] NVR               = new byte[NVRSIZE];
	static boolean issecurityactivated = true;	

	
	
	protected TheApplet() {
		byte[] writepin={(byte) 0x31, (byte) 0x31, (byte) 0x31, (byte) 0x31};        
		writepincode = new OwnerPIN((byte)3,(byte)8);  			// 3 tries 8=Max Size
		writepincode.update(writepin,(short)0,(byte)4); 			// from pincode, offset 0, length 4
		byte[] readpin = {(byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30};
		readpincode = new OwnerPIN((byte)3,(byte)8); 
		readpincode.update(readpin,(short)0,(byte)4); 			// 3 tries 8=Max Size
		

		this.register();
	}


	public static void install(byte[] bArray, short bOffset, byte bLength) throws ISOException {
		new TheApplet();
	} 


	public boolean select(){
		if ( writepincode.getTriesRemaining() == 0 || readpincode.getTriesRemaining()==0 ){
			return false;
		}
		else{
			return true;
		}
	}


	public void deselect(){
		writepincode.reset();
		readpincode.reset();
	}

	void verify( APDU apdu,OwnerPIN pin ) {
		apdu.setIncomingAndReceive();
		byte[] buffer = apdu.getBuffer();
		if( !pin.check( buffer, (byte)5, buffer[4] ) ) 
			ISOException.throwIt( SW_VERIFICATION_FAILED );
	}


	public void process(APDU apdu) throws ISOException {
		if( selectingApplet() == true )
			return;

		byte[] buffer = apdu.getBuffer();

		switch( buffer[1] ) 	{
			case UPDATECARDKEY: updateCardKey( apdu ); break;
			case UNCIPHERFILEBYCARD: uncipherFileByCard( apdu ); break;
			case CIPHERFILEBYCARD: cipherFileByCard( apdu ); break;
			case CIPHERANDUNCIPHERNAMEBYCARD: cipherAndUncipherNameByCard( apdu ); break;
			case READFILEFROMCARD: readFileFromCard( apdu ); break;
			case WRITEFILETOCARD: writeFileToCard( apdu ); break;
			case UPDATEWRITEPIN: updateWritePIN( apdu ); break;
			case UPDATEREADPIN: updateReadPIN( apdu ); break;
			case DISPLAYPINSECURITY: displayPINSecurity( apdu ); break;
			case DESACTIVATEACTIVATEPINSECURITY: desactivateActivatePINSecurity( apdu ); break;
			case ENTERREADPIN: enterReadPIN( apdu ); break;
			case ENTERWRITEPIN: enterWritePIN( apdu ); break;
			case READNAMEFROMCARD: readNameFromCard( apdu ); break;
			case WRITENAMETOCARD: writeNameToCard( apdu ); break;
			default: ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}


	void updateCardKey( APDU apdu ) {
	}


	void uncipherFileByCard( APDU apdu ) {
	}


	void cipherFileByCard( APDU apdu ) {
	}


	void cipherAndUncipherNameByCard( APDU apdu ) {
	}


	void readFileFromCard( APDU apdu ) {
		if(issecurityactivated) {if ( ! readpincode.isValidated() ){ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);}}
		byte[] buffer = apdu.getBuffer();
		Util.arrayCopy(NVR, (short)1, buffer, (short)0, NVR[0]);
		apdu.setOutgoingAndSend( (short)0, NVR[0] );
	}


	void writeFileToCard( APDU apdu ) {
		if(issecurityactivated) {if ( ! writepincode.isValidated() ){ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);}}
		byte[] buffer = apdu.getBuffer();
		apdu.setIncomingAndReceive();
		Util.arrayCopy(buffer,(short)4,NVR,(short)0,(short)(buffer[4]+1));
	}


	void updateWritePIN( APDU apdu ) {
		if(issecurityactivated) {if ( ! writepincode.isValidated() ){ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);}}
		byte[] buffer = apdu.getBuffer();
		apdu.setIncomingAndReceive();
		writepincode.update(buffer,(short)5,(byte)buffer[4]); 
	}


	void updateReadPIN( APDU apdu ) {
		if(issecurityactivated) {if ( ! readpincode.isValidated() ){ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);}}
		byte[] buffer = apdu.getBuffer();
		apdu.setIncomingAndReceive();
		readpincode.update(buffer,(short)5,(byte)buffer[4]); 
		
	}


	void displayPINSecurity( APDU apdu ) {
		byte[] buffer = apdu.getBuffer();
		buffer[0]=buffer[1]=(issecurityactivated?(byte)1:(byte)0);
		apdu.setOutgoingAndSend( (short)0, (short)2 );		
		

	}


	void desactivateActivatePINSecurity( APDU apdu ) {
		if(issecurityactivated==true) {issecurityactivated=false;}
		else{ issecurityactivated=true;}
	}


	void enterReadPIN( APDU apdu ) {
		
		verify(apdu,readpincode);
	}


	void enterWritePIN( APDU apdu ) {
						
		verify(apdu,writepincode);
	}


	void readNameFromCard( APDU apdu ) {
		if(issecurityactivated) {if ( ! readpincode.isValidated() ){ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);}}
		byte[] buffer = apdu.getBuffer();
		Util.arrayCopy(NVR, (short)1, buffer, (short)0, NVR[0]);
		apdu.setOutgoingAndSend( (short)0, NVR[0] );
		
	}


	void writeNameToCard( APDU apdu ) {
		if(issecurityactivated) {if ( ! writepincode.isValidated() ){ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);}}
		byte buffer [] = apdu.getBuffer();
		apdu.setIncomingAndReceive();
		Util.arrayCopy(buffer,(short)4,NVR,(short)0,(short)(buffer[4]+1));
	}


}
