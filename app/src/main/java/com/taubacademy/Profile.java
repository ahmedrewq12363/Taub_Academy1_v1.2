package com.taubacademy;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.net.URL;

public class Profile extends android.support.v4.app.Fragment {
    public ViewPager viewPager;
    Tutor tutor;
    private View profile;
    private PagerSlidingTabStrip pagerSlidingTabStrip;
    public interface  ClickListner{
        public void logOut();
    }
    ClickListner clickListner;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        clickListner=(ClickListner)activity;
    }

    public Profile() {
        super();
    }


    public Profile(Tutor tutor) {
        this.tutor = tutor;
        try {
            this.tutor.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewPager = (ViewPager) profile.findViewById(R.id.pager);
        Thread th = new Thread() {
            @Override
            public void run() {
                pagerSlidingTabStrip = (PagerSlidingTabStrip) profile.findViewById(R.id.Tabs);
                viewPager.setAdapter(new AdapterFeedBack(getChildFragmentManager(), tutor));
                pagerSlidingTabStrip.setViewPager(viewPager);
            }
        };
        th.run();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        profile = inflater.inflate(R.layout.fragment_profile, container, false);
        TextView Name = (TextView) profile.findViewById(R.id.NameOnPRo);
        ParseImageView imagePro = (ParseImageView) profile.findViewById(R.id.imageView);
        ParseFile imageFile = tutor.getPhotoFile();
        if (imageFile != null) {
            Picasso.with(getActivity().getBaseContext()).load(imageFile.getUrl()).into(imagePro);
        } else {
            URL url = null;
            try {
                url =  new URL("https://graph.facebook.com/" + tutor.get("UserId") + "/picture?type=large");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            Picasso.with(getActivity().getBaseContext()).load(url.toString()).into(imagePro);
        }
        Name.setText(tutor.getName());
        Integer salary = tutor.getSalary() == null ? 0 : tutor.getSalary();
        ((TextView) profile.findViewById(R.id.textView2)).setText(salary.toString());
        ImageButton edit = (ImageButton) profile.findViewById(R.id.imageButtonEdit);
        ImageButton logOut = (ImageButton) profile.findViewById(R.id.imageButtonLogOut);
        ImageView mail = (ImageView) profile.findViewById(R.id.EmailImage);
        ImageView phone = (ImageView) profile.findViewById(R.id.PhoneImage);
        RatingBar bar = (RatingBar) profile.findViewById(R.id.ratingBar);
        bar.setRating(tutor.getRating());
        bar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(final RatingBar ratingBar, float v, boolean b) {
                new Runnable() {

                    @Override
                    public void run() {
                        tutor.setRating((Tutor) ParseUser.getCurrentUser().get("Tutor"), ((Float) ratingBar.getRating()).intValue());
                        ratingBar.setRating(tutor.getRating());
                        tutor.saveInBackground();
                    }
                }.run();


            }
        });
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction Transaction = getFragmentManager().beginTransaction();
                Transaction.add(R.id.ProfileFrag, new MyProfileFragment(), null);
                Transaction.addToBackStack(null);
                Transaction.commit();
            }
        });
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            if(clickListner!=null){
                clickListner.logOut();
            }

                ParseUser.logOut();
                FragmentTransaction Transaction = getFragmentManager().beginTransaction();
                Transaction.setCustomAnimations(R.anim.animated_fragment, R.anim.animated_fragment2);
                Configuration config = getResources().getConfiguration();
                getFragmentManager().popBackStackImmediate();
                if ((config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) ==
                        Configuration.SCREENLAYOUT_SIZE_NORMAL) {
                    Transaction.setCustomAnimations(R.anim.animated_fragment, R.anim.animated_fragment2);
                    Transaction.replace(R.id.ProfileFrag,  new Describtion(), "Describtions");
                    Transaction.replace(R.id.ProfileFrag, new CoursesList(), "Courses");
                    Transaction.addToBackStack(null);
                    Transaction.commit();
                } else {
                    Transaction.setCustomAnimations(R.anim.animated_fragment, R.anim.animated_fragment2);
                    Transaction.replace(R.id.CoursesFrag, new CoursesList(), "Courses");
                    Transaction.replace(R.id.DescFrag, new Describtion(), "Describtions");
                    Transaction.addToBackStack(null);
                    Transaction.commit();
                }


            }
        });
        if ((ParseUser.getCurrentUser().get("Tutor") != null) && ParseUser.getCurrentUser().get("Tutor").equals(tutor)) {
            mail.setVisibility(View.INVISIBLE);
            phone.setVisibility(View.INVISIBLE);
            logOut.setVisibility(View.VISIBLE);
            edit.setVisibility(View.VISIBLE);
        } else {
            mail.setVisibility(View.VISIBLE);
            phone.setVisibility(View.VISIBLE);
            logOut.setVisibility(View.GONE);
            edit.setVisibility(View.GONE);
        }
        return profile;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


}

class AdapterFeedBack extends FragmentPagerAdapter {
    public Fragment[] fragment = new Fragment[3];
    public Tutor tutor;

    public AdapterFeedBack(FragmentManager manager, Tutor tutor) {
        super(manager);
        this.tutor = tutor;
        fragment[2] = TaughtByCourses.newInstance(tutor
        );
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Available On";
            case 1:
                return "Feedbacks";
            default:
                return "Relevant Courses";
        }
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                fragment[position] = AvailableOnFagment.newInstance(tutor);
                return fragment[position];
            case 1:
                fragment[position] = FeedBacksRecyclerView.newInstance(tutor);
                return fragment[position];
            default:

                return fragment[position];
        }
    }

};


