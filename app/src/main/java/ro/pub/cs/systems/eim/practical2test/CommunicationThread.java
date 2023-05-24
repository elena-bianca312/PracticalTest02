package ro.pub.cs.systems.eim.practical2test;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;


public class CommunicationThread extends Thread {

    private final ServerThread serverThread;
    private final Socket socket;

    // Constructor of the thread, which takes a ServerThread and a Socket as parameters
    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    // run() method: The run method is the entry point for the thread when it starts executing.
    // It's responsible for reading data from the client, interacting with the server,
    // and sending a response back to the client.
    @Override
    public void run() {
        // It first checks whether the socket is null, and if so, it logs an error and returns.
        if (socket == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            // Create BufferedReader and PrintWriter instances for reading from and writing to the socket
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client (pokemon) type!");

            // Read the city and informationType values sent by the client
            String pokemonName = bufferedReader.readLine();
            if (pokemonName == null || pokemonName.isEmpty()) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client (pokemon) type!");
                return;
            }

            // It checks whether the serverThread has already received the pokemon forecast information for the given city.
            HashMap<String, PokemonInformation> data = serverThread.getData();
            PokemonInformation pokemonInformation;
            if (data.containsKey(pokemonName)) {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
                pokemonInformation = data.get(pokemonName);
            } else {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");
                HttpClient httpClient = new DefaultHttpClient();
                String pageSourceCode = "";

                // make the HTTP request to the web service
                HttpGet httpGet = new HttpGet("https://pokeapi.co/api/v2/pokemon/" + pokemonName);
                HttpResponse httpGetResponse = httpClient.execute(httpGet);
                HttpEntity httpGetEntity = httpGetResponse.getEntity();
                if (httpGetEntity != null) {
                    pageSourceCode = EntityUtils.toString(httpGetEntity);
                }
                if (pageSourceCode == null) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                    return;
                } else Log.i(Constants.TAG, pageSourceCode);

                // Parse the page source code into a JSONObject and extract the needed information
                JSONObject content = new JSONObject(pageSourceCode);
                JSONArray pokemonAbilities = content.getJSONArray("abilities");

                JSONObject pokemonAbility;
                String[] abilities = new String[pokemonAbilities.length()];
                for (int i = 0; i < pokemonAbilities.length(); i++) {
                    pokemonAbility = pokemonAbilities.getJSONObject(i);
                    JSONObject abilityInfo = pokemonAbility.getJSONObject("ability");
                    String abilityName = abilityInfo.getString("name");
                    abilities[i] = abilityName;
                }

                JSONArray pokemonType = content.getJSONArray("types");
                JSONObject pokemonType1 = pokemonType.getJSONObject(0).getJSONObject("type");
                String typeName = pokemonType1.getString("name");
                System.out.println(typeName);

//                JSONArray pokemonURL = content.getJSONArray("sprites");
//                JSONObject pokemonURL1 = pokemonType.getJSONObject(0).getJSONObject("front_default");
//                String url = pokemonType1.getString("name");
//                System.out.println(url);

                // Create a PokemonInformation object with the information extracted from the JSONObject
                pokemonInformation = new PokemonInformation(abilities, typeName, null);

                // Cache the information for the given city
                serverThread.setData(pokemonName, pokemonInformation);
            }

            if (pokemonInformation == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Pokemon Information is null!");
                return;
            }

            // Send the information back to the client
            String result = "Abilities: \n" + pokemonInformation.getAbilities() + "\n" +
                    "Type: \n" + pokemonInformation.getType() + "\n";
            System.out.println(result);

            // Send the result back to the client
            printWriter.println(result);
            printWriter.flush();
        } catch (IOException | JSONException ioException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } finally {
            try {
                socket.close();
            } catch (IOException ioException) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                if (Constants.DEBUG) {
                    ioException.printStackTrace();
                }
            }
        }
    }

}
