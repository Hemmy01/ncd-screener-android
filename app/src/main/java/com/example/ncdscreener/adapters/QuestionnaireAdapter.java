package com.example.ncdscreener.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ncdscreener.R;
import com.example.ncdscreener.model.Questionnaire;

import java.util.List;

/**
 * RecyclerView Adapter for displaying questionnaire responses
 */
public class QuestionnaireAdapter extends RecyclerView.Adapter<QuestionnaireAdapter.QuestionnaireViewHolder> {

    private List<Questionnaire> questionnaires;

    public QuestionnaireAdapter(List<Questionnaire> questionnaires) {
        this.questionnaires = questionnaires;
    }

    @NonNull
    @Override
    public QuestionnaireViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_questionnaire, parent, false);
        return new QuestionnaireViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionnaireViewHolder holder, int position) {
        Questionnaire questionnaire = questionnaires.get(position);
        holder.bind(questionnaire);
    }

    @Override
    public int getItemCount() {
        return questionnaires != null ? questionnaires.size() : 0;
    }

    public void updateQuestionnaires(List<Questionnaire> newQuestionnaires) {
        this.questionnaires = newQuestionnaires;
        notifyDataSetChanged();
    }

    class QuestionnaireViewHolder extends RecyclerView.ViewHolder {
        private TextView textQuestionCode;
        private TextView textAnswer;

        public QuestionnaireViewHolder(@NonNull View itemView) {
            super(itemView);
            textQuestionCode = itemView.findViewById(R.id.text_question_code);
            textAnswer = itemView.findViewById(R.id.text_answer);
        }

        public void bind(Questionnaire questionnaire) {
            String questionLabel = formatQuestionCode(questionnaire.getQuestionCode());
            textQuestionCode.setText(questionLabel);
            
            String answer = questionnaire.getAnswer();
            String displayAnswer = "Yes".equalsIgnoreCase(answer) ? "Yes" : "No".equalsIgnoreCase(answer) ? "No" : answer;
            textAnswer.setText("Answer: " + displayAnswer);
        }

        private String formatQuestionCode(String code) {
            if (code == null) return "Unknown Question";
            
            // Convert snake_case to Title Case
            String[] parts = code.split("_");
            StringBuilder sb = new StringBuilder();
            for (String part : parts) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(part.substring(0, 1).toUpperCase());
                if (part.length() > 1) {
                    sb.append(part.substring(1).toLowerCase());
                }
            }
            return sb.toString();
        }
    }
}
