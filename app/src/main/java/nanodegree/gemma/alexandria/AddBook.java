package nanodegree.gemma.alexandria;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import nanodegree.gemma.alexandria.data.AlexandriaContract;
import nanodegree.gemma.alexandria.services.BookService;
import nanodegree.gemma.alexandria.services.DownloadImage;


public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "INTENT_TO_SCAN_ACTIVITY";
    private EditText ean;
    private final int LOADER_ID = 1;
    private View rootView;
    private final String EAN_CONTENT="eanContent";
//    private static final String SCAN_FORMAT = "scanFormat";
//    private static final String SCAN_CONTENTS = "scanContents";
//
//    private String mScanFormat = "Format:";
//    private String mScanContents = "Contents:";

    private static final int RC_BARCODE_CAPTURE = 0001;
    private CoordinatorLayout mCoordinator;
    private Snackbar mNoInternetSnackbar;
    private CardView mResultsCardView;

    public AddBook(){
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(ean!=null) {
            outState.putString(EAN_CONTENT, ean.getText().toString());
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        ean = (EditText) rootView.findViewById(R.id.ean);
        mCoordinator = (CoordinatorLayout)rootView.findViewById(R.id.myCoordinatorLayout);
        mResultsCardView = (CardView)rootView.findViewById(R.id.results_cardview);

        ean.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                String ean = s.toString();
                //catch isbn10 numbers
                if (ean.length() == 10 && !ean.startsWith("978")) {
                    ean = "978" + ean;
                }
                if (ean.length() < 13) {
                    clearFields();
                    return;
                }
                // ERROR found: now check for internet connection
                ConnectivityManager cm =
                        (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isConnectedOrConnecting()) {
//                    Log.d(TAG, "Internet connection OK before getting book info online");
                    //Once we have an ISBN, start a book intent
                    Intent bookIntent = new Intent(getActivity(), BookService.class);
                    bookIntent.putExtra(BookService.EAN, ean);
                    bookIntent.setAction(BookService.FETCH_BOOK);
                    getActivity().startService(bookIntent);
                    AddBook.this.restartLoader();
                } else {
                    mNoInternetSnackbar = Snackbar.make(mCoordinator,
                            getString(R.string.no_internet_connection_error), Snackbar.LENGTH_INDEFINITE);
                    mNoInternetSnackbar.setAction(R.string.ok, new MyAcknowledgeListener());
                    // a11y
                    mNoInternetSnackbar.setCallback(new Snackbar.Callback() {
                        @Override
                        public void onShown(Snackbar snackbar) {
                            mNoInternetSnackbar.getView().setContentDescription(getString(R.string.no_internet_connection_error));
                            mNoInternetSnackbar.getView().sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT);
                        }
                    });
                    mNoInternetSnackbar.show();
//                    Log.d(TAG, "No internet connection to get the book info!");
                }
            }
        });





        rootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Context context = getActivity();
                // launch barcode activity.
                Intent intent = new Intent(context, BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
                intent.putExtra(BarcodeCaptureActivity.UseFlash, false);

                startActivityForResult(intent, RC_BARCODE_CAPTURE);

            }
        });

        rootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ean.setText("");
            }
        });

        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean.getText().toString());
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                ean.setText("");
            }
        });

        if(savedInstanceState!=null){
            ean.setText(savedInstanceState.getString(EAN_CONTENT));
            ean.setHint("");
        }

        return rootView;
    }

    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(ean.getText().length()==0){
            return null;
        }
        String eanStr= ean.getText().toString();
        if(eanStr.length()==10 && !eanStr.startsWith("978")){
            eanStr="978"+eanStr;
        }
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText(bookTitle);

        // a11y
        String bookTitleDescription = getActivity().getResources().getString(R.string.book_found) + " " + bookTitle;
        ((TextView) rootView.findViewById(R.id.bookTitle)).setContentDescription(bookTitleDescription);
        ((TextView) rootView.findViewById(R.id.bookTitle)).requestFocus();


        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText(bookSubTitle);
        // a11y
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setContentDescription(bookSubTitle);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
//        authors = null;
        if (authors != null) {
            String[] authorsArr = authors.split(",");
            ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
            ((TextView) rootView.findViewById(R.id.authors)).setText(authors.replace(",", "\n"));
            // a11y
            ((TextView) rootView.findViewById(R.id.authors)).setContentDescription(getActivity().getResources().getString(R.string.book_authors) + " " + authors.replace(",", "\n"));
        }

        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if(Patterns.WEB_URL.matcher(imgUrl).matches()){
            new DownloadImage((ImageView) rootView.findViewById(R.id.bookCover)).execute(imgUrl);
            rootView.findViewById(R.id.bookCover).setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) rootView.findViewById(R.id.categories)).setText(categories);
        // a11y
        ((TextView) rootView.findViewById(R.id.categories)).setContentDescription(getActivity().getResources().getString(R.string.book_category) + " " + categories);

        rootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
        mResultsCardView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    private void clearFields(){
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.authors)).setText("");
        ((TextView) rootView.findViewById(R.id.categories)).setText("");
        //a117
        ((TextView) rootView.findViewById(R.id.bookTitle)).setContentDescription("");
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setContentDescription("");
        ((TextView) rootView.findViewById(R.id.authors)).setContentDescription("");
        ((TextView) rootView.findViewById(R.id.categories)).setContentDescription("");

        rootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
        mResultsCardView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.scan);
    }

    /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it.  The <var>resultCode</var> will be
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getActivity();
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    final Snackbar mySnackbar = Snackbar.make(mCoordinator, getText(R.string.barcode_success), Snackbar.LENGTH_SHORT);
//                    statusMessage.setText(R.string.barcode_success);
                    ean.setText(barcode.displayValue);
//                    Log.d(TAG, "Barcode read: " + barcode.displayValue);
                    mySnackbar.show();
                } else {
//                    statusMessage.setText(R.string.barcode_failure);
                    final Snackbar mySnackbar = Snackbar.make(mCoordinator, getText(R.string.barcode_failure), Snackbar.LENGTH_SHORT);
                    // a11y
                    mySnackbar.setCallback(new Snackbar.Callback() {
                        @Override
                        public void onShown(Snackbar snackbar) {
                            mySnackbar.getView().setContentDescription(getText(R.string.barcode_failure));
                            mySnackbar.getView().sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT);
                        }
                    });
                    mySnackbar.show();
//                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
//                statusMessage.setText(String.format(getString(R.string.barcode_error),
//                        CommonStatusCodes.getStatusCodeString(resultCode)));
                final Snackbar mySnackbar = Snackbar.make(mCoordinator,
                        String.format(getString(R.string.barcode_error), CommonStatusCodes.getStatusCodeString(resultCode)), Snackbar.LENGTH_SHORT);
                // a11y
                mySnackbar.setCallback(new Snackbar.Callback() {
                    @Override
                    public void onShown(Snackbar snackbar) {
                        mySnackbar.getView().setContentDescription(getText(R.string.barcode_error));
                        mySnackbar.getView().sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT);
                    }
                });
                mySnackbar.show();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Using the TextInputLayout > EditText setError
     * @param message
     */
    public void setError(String message) {
        ean.setError(message);
    }

    /**
     * Action that happens when user acknowlegdes the internet connection needed warning
     */
    public class MyAcknowledgeListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            mNoInternetSnackbar.dismiss();
        }
    }



}


