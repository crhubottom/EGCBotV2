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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

import static com.egc.bot.Bot.*;

public class AIController {
    public String gptCall(String prompt, String model) {
        OpenAI openAI = OpenAI.newBuilder(keys.get("OPENAI_KEY")).build();
        ChatClient chatClient = openAI.chatClient();
        CreateChatCompletionRequest createChatCompletionRequest = CreateChatCompletionRequest.newBuilder()
                .model(model)
                .message(ChatMessage.userMessage(prompt))
                .build();
        ChatCompletion chatCompletion = chatClient.createChatCompletion(createChatCompletionRequest);
        String out = chatCompletion.toString();
        //System.out.println(out);
        out = out.substring(out.indexOf("content=") + 8, out.lastIndexOf(", refusal"));
        return out;
    }

    public String gptCallWithSystem(String prompt, String systemPrompt, String model) {
        OpenAI openAI = OpenAI.newBuilder(keys.get("OPENAI_KEY")).build();
        ChatClient chatClient = openAI.chatClient();
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.systemMessage(systemPrompt));
        messages.add(ChatMessage.userMessage(prompt));
        CreateChatCompletionRequest createChatCompletionRequest = CreateChatCompletionRequest.newBuilder()
                .model(model)
                .messages(messages)
                .build();
        ChatCompletion chatCompletion = chatClient.createChatCompletion(createChatCompletionRequest);
        String out = chatCompletion.toString();
        out = out.substring(out.indexOf("content=") + 8, out.lastIndexOf(", refusal"));
        return out;
    }

    public static String visionCall(String prompt, String fileName) {
        StringBuilder out = new StringBuilder();
        var openAI = SimpleOpenAI.builder()
                .apiKey(keys.get("OPENAI_KEY"))
                .build();

        var chatRequest = ChatRequest.builder()
                .model(textModel)
                .messages(List.of(
                        io.github.sashirestela.openai.domain.chat.ChatMessage.UserMessage.of(List.of(
                                ContentPart.ContentPartText.of(
                                        prompt),
                                ContentPart.ContentPartImageUrl.of(loadImageAsBase64(fileName))))))
                .build();
        var chatResponse = openAI.chatCompletions().createStream(chatRequest).join();
        chatResponse.filter(chatResp -> chatResp.getChoices().size() > 0 && chatResp.firstContent() != null)
                .map(Chat::firstContent)
                .forEach(out::append);

        return out.toString();
    }

    private static ContentPart.ContentPartImageUrl.ImageUrl loadImageAsBase64(String imagePath) {
        try {
            Path path = Paths.get(imagePath);
            byte[] imageBytes = Files.readAllBytes(path);
            String base64String = Base64.getEncoder().encodeToString(imageBytes);
            var extension = imagePath.substring(imagePath.lastIndexOf('.') + 1);
            var prefix = "data:image/" + extension + ";base64,";
            return ContentPart.ContentPartImageUrl.ImageUrl.of(prefix + base64String);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void dalleCall(String prompt, String fileName)  {
        String apiKey = keys.get("OPENAI_KEY");  // Your actual API key

        // JSON payload requesting base64
        String jsonPayload = """
            {
              "model": "gpt-image-1.5",
              "prompt": "%s",
              "n": 1,
              "size": "1024x1024"
            }
            """.formatted(prompt);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/images/generations"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
try {
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    String responseBody = response.body();


    // Parse with JSON
    JSONObject json = new JSONObject(responseBody);
    JSONArray dataArray = json.getJSONArray("data");
    String b64String = dataArray.getJSONObject(0).getString("b64_json");

    // Clean & decode
    byte[] decodedBytes = Base64.getDecoder().decode(b64String);
    Files.write(Paths.get(fileName+".png"), decodedBytes);

    System.out.println("Image saved as " + fileName);
}catch (Exception e){
    e.printStackTrace();
}

    }
    public static class Voice {
        public String voice_id;
        public String name;
    }

    public static class VoicesResponse {
        public List<Voice> voices;
    }
    public boolean ttsCall(String prompt, String fileName) throws IOException, InterruptedException {




        Random random = new Random();
        int voiceNum=random.nextInt(voiceArray.length);
        String voiceId = voiceArray[voiceNum].voice_id;
        String voiceName = voiceArray[voiceNum].name;
        String url = "https://api.elevenlabs.io/v1/text-to-speech/" + voiceId
                + "?output_format=mp3_44100_128";

        String json = """
            {
              "text": "%s",
              "model_id": "eleven_v3"
            }
            """.formatted(prompt);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("xi-api-key", ElevenLabsapiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<byte[]> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofByteArray()
        );

        if (response.statusCode() == 200) {
            Files.write(Path.of(fileName+".mp3"), response.body());
            System.out.println("Saved "+fileName+".mp3");
            return true;
        } else {
            System.out.println("Request failed: " + response.statusCode());
            System.out.println(new String(response.body()));
            return false;
        }
    }


    public String deepgramSpeechToText(File filename) {
        try {
            // Specify the URL for the Deepgram API endpoint
            URI uri = new URI("https://api.deepgram.com/v1/listen?model=nova");

            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();

            // Set the request method to POST
            connection.setRequestMethod("POST");

            // Set request headers
            String token="Token "+deepKey;
            //System.out.println(token);
            //connection.setRequestProperty("Authorization", "Token 293bd934719aab9d97c8a04235f525e9def08500");  // Replace YOUR_DEEPGRAM_API_KEY
            connection.setRequestProperty("Authorization", token);  // Replace YOUR_DEEPGRAM_API_KEY

            // with your actual API key
            connection.setRequestProperty("Content-Type", "audio/wav");

            // Enable output (sending data to the server)
            connection.setDoOutput(true);

            // Get the output stream of the connection
            OutputStream outputStream = connection.getOutputStream();

            // Read the audio file as binary data and write it to the output stream
            FileInputStream fileInputStream = new FileInputStream(filename.getPath()); // Replace "youraudio.wav" with the path
            // to your audio file
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            fileInputStream.close();

            // Close the output stream
            outputStream.close();

            // Get the response code from the server
            int responseCode = connection.getResponseCode();

            // Check if the request was successful (status code 200)
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read and print the response from the server
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                System.out.println(response.substring(response.indexOf("transcript")+13, response.indexOf("confidence")-3));
                return response.substring(response.indexOf("transcript")+13, response.indexOf("confidence")-3);
            } else {
                System.out.println("HTTP request failed with status code " + responseCode);
            }

            // Disconnect the connection
            connection.disconnect();
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }


}
