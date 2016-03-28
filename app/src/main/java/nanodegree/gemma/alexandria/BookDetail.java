package nanodegree.gemma.alexandria;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import nanodegree.gemma.alexandria.data.AlexandriaContract;
import nanodegree.gemma.alexandria.services.BookService;
import nanodegree.gemma.alexandria.services.DownloadImage;


public class BookDetail extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EAN_KEY = "EAN";
    private final int LOADER_ID = 10;
    private View rootView;
    private String ean;
    private String bookTitle;
    private ShareActionProvider shareActionProvider;

    public BookDetail(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            ean = arguments.getString(BookDetail.EAN_KEY);
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }

        rootView = inflater.inflate(R.layout.fragment_full_book, container, false);
        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean);
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        return rootView;
    }

    private void finishCreatingMenu(Menu menu) {
        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);
        // Get the provider and hold onto it to set/change the share intent.
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
//        Log.d("BookDetail", "finishCreatingMenu menu item is " + menuItem);
        menuItem.setIntent(createShareBookIntent());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // ERROR found: clear the menu to prevent menu inflating twice
        menu.clear();
        inflater.inflate(R.menu.book_detail, menu);
//        MenuItem menuItem = menu.findItem(R.id.action_share);
//        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
            finishCreatingMenu(menu);
    }

    /**
     * Creates the share book intent
     * @return
     */
    private Intent createShareBookIntent() {
//        Log.d("BookDetail", "createShareBookIntent");
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) +" "+ bookTitle);
        return shareIntent;
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(ean)),
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

        bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        TextView titleView = ((TextView) rootView.findViewById(R.id.fullBookTitle));
        titleView.setText(bookTitle);
        titleView.requestFocus();

        // a11y
        String bookTitleDescription = getActivity().getResources().getString(R.string.book_detail) + " " + bookTitle;
        titleView.setContentDescription(bookTitleDescription);
//        Log.d("A11y", bookTitleDescription);

        // ERROR on orientation change, moved the menu creation to a separate function so we can call it separately
//        Intent shareIntent = new Intent(Intent.ACTION_SEND);
//        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
//        shareIntent.setType("text/plain");
//        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) +" "+ bookTitle);
//        shareActionProvider.setShareIntent(shareIntent);

        // If onCreateOptionsMenu has already happened, we need to update the share intent now with the book details
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(createShareBookIntent());
        }

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        TextView subtitleView = ((TextView) rootView.findViewById(R.id.fullBookSubTitle));
        subtitleView.setText(bookSubTitle);
        // a11y
        subtitleView.setContentDescription(bookSubTitle);

        String desc = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.DESC));
        TextView descriptionView = ((TextView) rootView.findViewById(R.id.fullBookDesc));
        descriptionView.setText(desc);
        // a11y
        descriptionView.setContentDescription(desc);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        if (authors != null) {
            String[] authorsArr = authors.split(",");
            TextView authorsView = ((TextView) rootView.findViewById(R.id.authors));
            authorsView.setLines(authorsArr.length);
            authorsView.setText(authors.replace(",", "\n"));
            // a11y
            authorsView.setContentDescription(getActivity().getResources().getString(R.string.book_authors) + " " + authors.replace(",", "\n"));
        }
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if(Patterns.WEB_URL.matcher(imgUrl).matches()){
            new DownloadImage((ImageView) rootView.findViewById(R.id.fullBookCover)).execute(imgUrl);
            rootView.findViewById(R.id.fullBookCover).setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        TextView categoriesView = ((TextView) rootView.findViewById(R.id.categories));
        categoriesView.setText(categories);
        // a11y
        categoriesView.setContentDescription(getActivity().getResources().getString(R.string.book_category) + " "+categories);

        // Removed custom back button to comply with Android Guidelines
//        if(rootView.findViewById(R.id.right_container)!=null){
//            rootView.findViewById(R.id.backButton).setVisibility(View.INVISIBLE);
//        }

    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    @Override
    public void onPause() {
        super.onDestroyView();
        // ERROR found: pop the back stack if I have a right side fragment, this is if the right_container id is NOT null
        if(MainActivity.IS_TABLET && rootView.findViewById(R.id.right_container)!=null){
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }
}