package com.egc.bot;

import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.common.content.ContentPart;
import io.github.sashirestela.openai.domain.chat.Chat;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import io.github.stefanbratanov.jvm.openai.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.egc.bot.Bot.*;

public class AIController {

    // Reuse one HTTP client instead of creating a new one per request
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    public String gptCall(String prompt, String model) {
        OpenAI openAI = OpenAI.newBuilder(keys.get("OPENAI_KEY")).build();
        ChatClient chatClient = openAI.chatClient();
        CreateChatCompletionRequest request = CreateChatCompletionRequest.newBuilder()
                .model(model)
                .message(ChatMessage.userMessage(prompt))
                .build();
        ChatCompletion chatCompletion = chatClient.createChatCompletion(request);
        return firstContent(chatCompletion);
    }

    public String gptCallWithSystem(String prompt, String systemPrompt, String model) {
        OpenAI openAI = OpenAI.newBuilder(keys.get("OPENAI_KEY")).build();
        ChatClient chatClient = openAI.chatClient();
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.systemMessage(systemPrompt));
        messages.add(ChatMessage.userMessage(prompt));
        CreateChatCompletionRequest request = CreateChatCompletionRequest.newBuilder()
                .model(model)
                .messages(messages)
                .build();
        ChatCompletion chatCompletion = chatClient.createChatCompletion(request);
        return firstContent(chatCompletion);
    }

    // Read the reply from the response object instead of parsing toString()
    private static String firstContent(ChatCompletion completion) {
        if (completion.choices() == null || completion.choices().isEmpty()) {
            return "";
        }
        String content = completion.choices().get(0).message().content();
        return content != null ? content : "";
    }

    public static String visionCall(String prompt, String fileName) throws IOException {
        StringBuilder out = new StringBuilder();
        var openAI = SimpleOpenAI.builder()
                .apiKey(keys.get("OPENAI_KEY"))
                .build();

        var chatRequest = ChatRequest.builder()
                .model(textModel)
                .messages(List.of(
                        io.github.sashirestela.openai.domain.chat.ChatMessage.UserMessage.of(List.of(
                                ContentPart.ContentPartText.of(prompt),
                                ContentPart.ContentPartImageUrl.of(loadImageAsBase64(fileName))))))
                .build();
        var chatResponse = openAI.chatCompletions().createStream(chatRequest).join();
        chatResponse.filter(chatResp -> !chatResp.getChoices().isEmpty() && chatResp.firstContent() != null)
                .map(Chat::firstContent)
                .forEach(out::append);

        return out.toString();
    }

    // Throws instead of returning null, which used to cause an NPE further down
    private static ContentPart.ContentPartImageUrl.ImageUrl loadImageAsBase64(String imagePath) throws IOException {
        byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
        String base64String = Base64.getEncoder().encodeToString(imageBytes);
        String extension = imagePath.substring(imagePath.lastIndexOf('.') + 1).toLowerCase();
        if (extension.equals("jpg")) {
            extension = "jpeg"; // "image/jpg" is not a valid MIME type
        }
        String prefix = "data:image/" + extension + ";base64,";
        return ContentPart.ContentPartImageUrl.ImageUrl.of(prefix + base64String);
    }

    public void dalleCall(String prompt, String fileName) throws IOException, InterruptedException {
        String apiKey = keys.get("OPENAI_KEY");

        // Build JSON properly so quotes/newlines in the prompt don't break the request
        JSONObject payload = new JSONObject()
                .put("model", "gpt-image-2.5-flare")
                .put("prompt", prompt)
                .put("n", 1)
                .put("size", "1024x1024");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/images/generations"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject json = new JSONObject(response.body());

        // Surface the API's error message instead of silently printing a stack trace
        if (response.statusCode() != 200) {
            String message = json.has("error")
                    ? json.getJSONObject("error").optString("message", response.body())
                    : response.body();
            throw new IOException("Image API returned " + response.statusCode() + ": " + message);
        }

        JSONArray dataArray = json.optJSONArray("data");
        if (dataArray == null || dataArray.isEmpty() || !dataArray.getJSONObject(0).has("b64_json")) {
            throw new IOException("Image API response contained no image data.");
        }

        byte[] decodedBytes = Base64.getDecoder().decode(dataArray.getJSONObject(0).getString("b64_json"));
        Files.write(Paths.get(fileName + ".png"), decodedBytes);
        System.out.println("Image saved as " + fileName + ".png");
    }

    public static class Voice {
        public String voice_id;
        public String name;
    }

    public static class VoicesResponse {
        public List<Voice> voices;
    }

    public boolean ttsCall(String prompt, String fileName) throws IOException, InterruptedException {
        String voiceId;

        if (currentVoice.size() == 1 && Objects.equals(currentVoice.get(0), "Random")) {
            if (voiceArray == null || voiceArray.length == 0) {
                System.out.println("No voices loaded.");
                return false;
            }
            voiceId = voiceArray[rand.nextInt(voiceArray.length)].voice_id;
        } else {
            if (currentVoice.isEmpty()) {
                System.out.println("No voice selected.");
                return false;
            }
            String voiceName = currentVoice.get(rand.nextInt(currentVoice.size()));
            voiceId = voiceMap.get(voiceName);
            if (voiceId == null) {
                System.out.println("Unknown voice: " + voiceName);
                return false;
            }
        }

        String url = "https://api.elevenlabs.io/v1/text-to-speech/" + voiceId
                + "?output_format=mp3_44100_128";

        // Proper JSON escaping, same fix as dalleCall
        JSONObject payload = new JSONObject()
                .put("text", prompt)
                .put("model_id", "eleven_multilingual_v2");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("xi-api-key", ElevenLabsapiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        HttpResponse<byte[]> response = HTTP.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() == 200) {
            Files.write(Path.of(fileName + ".mp3"), response.body());
            System.out.println("Saved " + fileName + ".mp3");
            return true;
        } else {
            System.out.println("Request failed: " + response.statusCode());
            System.out.println(new String(response.body(), StandardCharsets.UTF_8));
            return false;
        }
    }

    public String deepgramSpeechToText(File file) {
        HttpURLConnection connection = null;
        try {
            URI uri = new URI("https://api.deepgram.com/v1/listen?model=nova");
            connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Token " + deepKey);
            connection.setRequestProperty("Content-Type", "audio/wav");
            connection.setDoOutput(true);

            // try-with-resources so streams close even if an exception is thrown
            try (OutputStream outputStream = connection.getOutputStream();
                 InputStream fileInputStream = new FileInputStream(file)) {
                fileInputStream.transferTo(outputStream);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.out.println("HTTP request failed with status code " + responseCode);
                InputStream err = connection.getErrorStream();
                if (err != null) {
                    try (err) {
                        System.out.println(new String(err.readAllBytes(), StandardCharsets.UTF_8));
                    }
                }
                return null;
            }

            String body;
            try (InputStream in = connection.getInputStream()) {
                body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }

            // Parse JSON instead of substring offsets
            String transcript = new JSONObject(body)
                    .getJSONObject("results")
                    .getJSONArray("channels").getJSONObject(0)
                    .getJSONArray("alternatives").getJSONObject(0)
                    .optString("transcript", "");
            System.out.println(transcript);
            return transcript;
        } catch (IOException | URISyntaxException | org.json.JSONException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect(); // previously skipped on the success path
            }
        }
    }
}