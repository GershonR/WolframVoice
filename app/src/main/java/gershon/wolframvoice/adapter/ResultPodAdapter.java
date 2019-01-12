package gershon.wolframvoice.adapter;

import android.content.ClipData;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import gershon.wolframvoice.R;
import gershon.wolframvoice.Utils;
import gershon.wolframvoice.model.ResultPod;

public class ResultPodAdapter extends RecyclerView.Adapter<ResultPodAdapter.ResultPodViewHolder> {

    private List<ResultPod> resultPods;
    private Context context;

    public ResultPodAdapter(List<ResultPod> resultPods) {
        this.resultPods = resultPods;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public ResultPodViewHolder onCreateViewHolder(ViewGroup parent, int i) {

        context = parent.getContext();

        // Set card layout

        View view = LayoutInflater.from(context).inflate(R.layout.result_pod_card_layout, parent, false);
        ResultPodViewHolder resultPodViewHolder = new ResultPodViewHolder(view);
        return resultPodViewHolder;
    }

    @Override
    public void onBindViewHolder(ResultPodViewHolder resultPodViewHolder, final int position) {

        resultPodViewHolder.textViewResultPodTitle.setText(resultPods.get(position).getTitle());
        resultPodViewHolder.textViewResultPodDescription.setText(resultPods.get(position).getDescription());

        if (StringUtils.isNotEmpty(resultPods.get(position).getImageSource())) {

            resultPodViewHolder.imageView.setVisibility(View.VISIBLE);

            Uri imageUri = Uri.parse(resultPods.get(position).getImageSource());

            resultPodViewHolder.imageView.setPadding(0, 0, 0, 0);
            resultPodViewHolder.imageView.setImageURI(imageUri);
            Picasso.get().load(imageUri).into(resultPodViewHolder.imageView);

        } else {
            resultPodViewHolder.imageView.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return resultPods.size();
    }


    public static class ResultPodViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView textViewResultPodTitle;
        TextView textViewResultPodDescription;
        ImageView imageView;

        ResultPodViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.result_pod_card_view);
            textViewResultPodTitle = (TextView) itemView.findViewById(R.id.text_result_pod_title);
            textViewResultPodDescription = (TextView) itemView.findViewById(R.id.text_result_pod_description);
            imageView = (ImageView) itemView.findViewById(R.id.image_result_pod);
        }
    }
}
