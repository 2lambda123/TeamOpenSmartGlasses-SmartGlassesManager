package com.smartglassesmanager.androidsmartphone.smartglassescommunicators;

import static com.vuzix.ultralite.LVGLImage.CF_INDEXED_2_BIT;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.smartglassesmanager.androidsmartphone.R;
import com.vuzix.ultralite.Anchor;
import com.vuzix.ultralite.LVGLImage;
import com.vuzix.ultralite.Layout;
import com.vuzix.ultralite.TextAlignment;
import com.vuzix.ultralite.TextWrapMode;
import com.vuzix.ultralite.UltraliteColor;
import com.vuzix.ultralite.UltraliteSDK;


//communicate with ActiveLook smart glasses
public class UltraliteSGC extends SmartGlassesCommunicator {
    private static final String TAG = "WearableAi_UltraliteSGC";

    UltraliteSDK ultraliteSdk;
    UltraliteSDK.Canvas ultraliteCanvas;
    LifecycleOwner lifecycleOwner;
    Context context;

    //handler to disconnect
    Handler killHandler;

    public UltraliteSGC(Context context, LifecycleOwner lifecycleOwner) {
        super();
        this.lifecycleOwner = lifecycleOwner;
        this.context = context;

        ultraliteSdk = UltraliteSDK.get(context);
//        ultraliteSdk.addEventListener(lifecycleOwner);
        LiveData<Boolean> ultraliteConnectedLive = ultraliteSdk.getConnected();
        ultraliteConnectedLive.observe(lifecycleOwner, isConnected -> {
            onUltraliteConnectedChange(isConnected);
        });

        LiveData<Boolean> ultraliteControlled = ultraliteSdk.getControlledByMe();
        ultraliteControlled.observe(lifecycleOwner, isControlled -> {
            onUltraliteControlChanged(isControlled);
        });

//        if (ultraliteSdk.isAvailable()){
//            Log.d(TAG, "Ultralite SDK is available.");
//        } else {
//            Log.d(TAG, "Ultralite SDK is NOT available.");
//        }

        //state information
        mConnectState = 0;

        killHandler = new Handler();
    }

    private void onUltraliteConnectedChange(boolean isConnected) {
        Log.d(TAG, "Ultralite connected: " + isConnected);
        if (isConnected && (mConnectState != 2)) {
            mConnectState = 2;
            connectionEvent(2);
            boolean isControlled = ultraliteSdk.requestControl();
            Log.d(TAG, "Ultralite control request: " + isControlled);
        } else {
            connectionEvent(0);
        }
    }

    private void onUltraliteControlChanged(boolean isControlledByMe) {
        Log.d(TAG, "Ultralite controlled by me: " + isControlledByMe);
        if(isControlledByMe) {
            setupUltraliteCanvas();
        }
//        mUltraliteControlledByMe = isControlledByMe;
    }

    @Override
    protected void setFontSizes(){
    }

    @Override
    public void connectToSmartGlasses(){
        Log.d(TAG, "connectToSmartGlasses running...");
//        while (mConnectState != 2){
//            Log.d(TAG, "Don't have Ultralite yet, let's wait for it...");
//            try {
//                Thread.sleep(200);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        Log.d(TAG, "Connected to Ultralites.");
//        connectionEvent(mConnectState);
        Log.d(TAG, "connectToSmartGlasses run");
    }

    public void displayTextLine(String text){
        displayReferenceCardSimple("", text);
    }

    public void displayCenteredText(String text){
    }

    public void showNaturalLanguageCommandScreen(String prompt, String naturalLanguageInput){
//        int boxDelta = 3;
//
//        if (connectedGlasses != null) {
//            connectedGlasses.clear();
//            showPromptCircle();
//
//            //show the prompt
//            lastLocNaturalLanguageArgsTextView = displayText(new TextLineSG(prompt, SMALL_FONT), new Point(0, 11), true);
//            lastLocNaturalLanguageArgsTextView = new Point(lastLocNaturalLanguageArgsTextView.x, lastLocNaturalLanguageArgsTextView.y + boxDelta); //margin down a tad
//
//            //show the final "finish command" prompt
//            int finishY = 90;
//            displayLine(new Point(0, finishY), new Point(100, finishY));
//            displayText(new TextLineSG(finishNaturalLanguageString, SMALL_FONT), new Point(0, finishY + 2), true);
//
//            //show the natural language args in a scroll box
////            ArrayList<TextLineSG> nli = new ArrayList<>();
////            nli.add(new TextLineSG(naturalLanguageInput, SMALL_FONT));
////            lastLocNaturalLanguageArgsTextView = scrollTextShow(nli, startScrollBoxY.y + boxDelta, finishY - boxDelta);
//        }
    }

    public void updateNaturalLanguageCommandScreen(String naturalLanguageArgs){
//        Log.d(TAG, "Displaynig: " + naturalLanguageArgs);
//        displayText(new TextLineSG(naturalLanguageArgs, SMALL_FONT), new Point(0, lastLocNaturalLanguageArgsTextView.y));
    }

    public void blankScreen(){
//        if (connectedGlasses != null){
//            connectedGlasses.clear();
//        }
    }

    @Override
    public void destroy(){
       if (ultraliteSdk != null){
           displayReferenceCardSimple("Disconnecting...", "Disconnecting Smart Glasses from SGM");

           //disconnect after slight delay, so our above text gets a chance to show up
           killHandler.postDelayed(new Runnable() {
               @Override
               public void run() {
                   ultraliteSdk.releaseControl();
               }
           }, 600);

       }
    }

    public void showHomeScreen(){
    }

    public void setupUltraliteCanvas(){
        ultraliteCanvas = ultraliteSdk.getCanvas();
    }

    public void changeUltraliteLayout(Layout chosenLayout) {
        ultraliteSdk.setLayout(chosenLayout, 0, false);
    }

    public void startScrollingTextViewMode(String title){
        super.startScrollingTextViewMode(title);

        if (ultraliteSdk == null) {
            return;
        }

        //clear the screen
        ultraliteCanvas.clear();
        drawTextOnUltralite(title);
    }

    public void drawTextOnUltralite(String text){
        //display the title at the top of the screen
        UltraliteColor ultraliteColor = UltraliteColor.WHITE;
        Anchor ultraliteAnchor = Anchor.TOP_LEFT;
        TextAlignment ultraliteAlignment = TextAlignment.LEFT;
        changeUltraliteLayout(Layout.CANVAS);
        ultraliteCanvas.clear();
//        ultraliteCanvas.createText(text, ultraliteAlignment, ultraliteColor, ultraliteAnchor, true);
        ultraliteCanvas.createText(text, ultraliteAlignment, ultraliteColor, ultraliteAnchor, 0, 0, -1, -1, TextWrapMode.WRAP, true);
        ultraliteCanvas.createText(text, ultraliteAlignment, ultraliteColor, Anchor.BOTTOM_RIGHT, 0, 0, -1, -1, TextWrapMode.WRAP, true);
        ultraliteCanvas.commitText();
        ultraliteCanvas.clearBackground(UltraliteColor.WHITE);
    }

    public Bitmap getBitmapFromDrawable(Resources res) {
        return BitmapFactory.decodeResource(res, R.drawable.vuzix_shield);
    }

    public void displayReferenceCardSimple(String title, String body){
        if (isConnected()) {
            Log.d(TAG, "Sending text to Ultralite SDK: " + title + "     " + body);
//            ultraliteSdk.sendText("hello world"); //this is BROKEN in Vuzix ultralite 0.4.2 SDK - crashes Vuzix OEM Platform android app

//            changeUltraliteLayout(Layout.DEFAULT);
//            ultraliteSdk.sendNotification(title, body);

            String newBody = "Lorem ipsum dolor sit amet, \n consectetur adipiscing elit. Morbi \n suscipit vitae libero sit amet finibus.";
            drawTextOnUltralite(newBody);

            //send image on ultralite
//            Anchor ultraliteAnchor = Anchor.TOP_LEFT;
//            LVGLImage ultraliteImage = LVGLImage.fromBitmap(getBitmapFromDrawable(context.getResources()), CF_INDEXED_2_BIT);
//            changeUltraliteLayout(Layout.CANVAS);
//            ultraliteCanvas.createImage(ultraliteImage, ultraliteAnchor, 0, 0, true);
        }
    }

    //don't show images on activelook (screen is too low res)
    public void displayReferenceCardImage(String title, String body, String imgUrl){
        displayReferenceCardSimple(title, body);
    }

    //handles text wrapping, returns final position of last line printed
//    private Point displayText(TextLineSG textLine, Point percentLoc, boolean centered){
//        if (!isConnected()){
//            return null;
//        }
//
//        //get info about the wrapping
//        Pair wrapInfo = computeStringWrapInfo(textLine);
//        int numWraps = (int)wrapInfo.first;
//        int wrapLenNumChars = (int)wrapInfo.second;
//
//        //loop through the text, writing out individual lines to the glasses
//        ArrayList<String> chunkedText = new ArrayList<>();
//        Point textPoint = percentLoc;
//        int textMarginY = computeMarginPercent(textLine.getFontSizeCode()); //(fontToSize.get(textLine.getFontSize()) * 1.3)
//        for (int i = 0; i <= numWraps; i++){
//            int startIdx = wrapLenNumChars * i;
//            int endIdx = Math.min(startIdx + wrapLenNumChars, textLine.getText().length());
//            String subText = textLine.getText().substring(startIdx, endIdx).trim();
//            chunkedText.add(subText);
//            TextLineSG thisTextLine = new TextLineSG(subText, textLine.getFontSizeCode());
//            if (!centered) {
//                sendTextToGlasses(thisTextLine, textPoint);
//            } else {
//                int xPercentLoc = computeStringCenterInfo(thisTextLine);
//                sendTextToGlasses(thisTextLine, new Point(xPercentLoc, textPoint.y));
//            }
//            textPoint = new Point(textPoint.x, textPoint.y + pixelToPercent(displayHeightPixels, fontToSize.get(textLine.getFontSizeCode())) + textMarginY); //lower our text for the next loop
//        }
//
//        return textPoint;
//    }

    public void stopScrollingTextViewMode() {
//        if (connectedGlasses == null) {
//            return;
//        }
//
//        //clear the screen
//        connectedGlasses.clear();
    }

    public void scrollingTextViewIntermediateText(String text){
    }

    public void scrollingTextViewFinalText(String text){
        if (!isConnected()){
            return;
        }

//        //save to our saved list of final scrolling text strings
//        finalScrollingTextStrings.add(text);
//
//        //get the max number of wraps allows
//        float allowedTextRows = computeAllowedTextRows(fontToSize.get(scrollingTextTitleFontSize), fontToSize.get(scrollingTextTextFontSize), percentToPixel(displayHeightPixels, computeMarginPercent(scrollingTextTextFontSize)));
//
//        //figure out the maximum we can display
//        int totalRows = 0;
//        ArrayList<String> finalTextToDisplay = new ArrayList<>();
//        boolean hitBottom = false;
//        for (int i = finalScrollingTextStrings.toArray().length - 1; i >= 0; i--){
//            String finalText = finalScrollingTextStrings.get(i);
//            //convert to a TextLine type with small font
//            TextLineSG tlString = new TextLineSG(finalText, SMALL_FONT);
//            //get info about the wrapping of this string
//            Pair wrapInfo = computeStringWrapInfo(tlString);
//            int numWraps = (int)wrapInfo.first;
//            int wrapLenNumChars = (int)wrapInfo.second;
//            totalRows += numWraps + 1;
//
//            if (totalRows > allowedTextRows){
//                finalScrollingTextStrings = finalTextToDisplay;
//                lastLocScrollingTextView = belowTitleLocScrollingTextView;
//                //clear the glasses as we hit our limit and need to redraw
//                connectedGlasses.color((byte)0x00);
//                connectedGlasses.rectf(percentScreenToPixelsLocation(belowTitleLocScrollingTextView.x, belowTitleLocScrollingTextView.y), percentScreenToPixelsLocation(100, 100));
//                //stop looping, as we've ran out of room
//                hitBottom = true;
//            } else {
//                finalTextToDisplay.add(0, finalText);
//            }
//        }
//
//        //display all of the text that we can
//        if (hitBottom) { //if we ran out of room, we need to redraw all the text
//            for (String finalString : finalTextToDisplay) {
//                TextLineSG tlString = new TextLineSG(finalString, scrollingTextTextFontSize);
//                //write this text at the last location + margin
//                Log.d(TAG, "Writing string: " + tlString.getText() + finalTextToDisplay.size());
//                lastLocScrollingTextView = displayText(tlString, new Point(0, lastLocScrollingTextView.y));
//            }
//        } else { //if we didn't hit the bottom, and there's room, we can just display the next line
//            TextLineSG tlString = new TextLineSG(text, scrollingTextTextFontSize);
//            lastLocScrollingTextView = displayText(tlString, new Point(0, lastLocScrollingTextView.y));
//        }

    }

    public void displayPromptView(String prompt, String [] options){
        if (!isConnected()){
            return;
        }

//        connectedGlasses.clear();
//        showPromptCircle();
//
//        //show the prompt and options, if any
//        ArrayList<Object> promptPageElements = new ArrayList<>();
//        promptPageElements.add(new TextLineSG(prompt, LARGE_FONT));
//        if (options != null) {
//            //make an array list of options
//            for (String s : options){
//               promptPageElements.add(new TextLineSG(s, SMALL_FONT));
//            }
//        }
//        displayLinearStuff(promptPageElements, new Point(0, 11), true);
    }
}
