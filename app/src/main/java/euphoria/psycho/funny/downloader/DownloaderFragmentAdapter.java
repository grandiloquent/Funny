package euphoria.psycho.funny.downloader;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class DownloaderFragmentAdapter extends FragmentPagerAdapter {
    private List<Fragment> mFragmentList;

    public DownloaderFragmentAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    public void setData(List<Fragment> fragmentList) {
        mFragmentList = fragmentList;
    }

    @Override
    public int getCount() {
        return mFragmentList == null ? 0 : mFragmentList.size();
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        if (mFragmentList == null) return "";
        if (position == 0) return "下载";
        return "已完成任务";
    }
}
