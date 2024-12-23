package pora.predstavitev.app

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment.DIRECTORY_MOVIES
import android.os.Environment.DIRECTORY_PICTURES
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.facebook.AccessToken
import com.facebook.AccessTokenTracker
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.GraphResponse
import com.facebook.LoginStatusCallback
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.model.SharePhoto
import com.facebook.share.model.SharePhotoContent
import com.facebook.share.model.ShareStoryContent
import com.facebook.share.model.ShareVideo
import com.facebook.share.model.ShareVideoContent
import org.json.JSONObject
import pora.predstavitev.app.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var accessTokenTracker: AccessTokenTracker
    private lateinit var demoApp: DemoApplication

    private fun setContentPhoto() {
        val bitmap: Bitmap = resources.getDrawable(R.drawable.rafting).toBitmap()

        val photo: SharePhoto = SharePhoto.Builder()
            .setCaption("${demoApp.userName} has gone rafting!")
            .setBitmap(bitmap)
            .build()

        val content: SharePhotoContent = SharePhotoContent.Builder()
            .addPhoto(photo)
            .build()

        binding.fbShareButton.shareContent = content
    }

    private fun setContentLink() {
        val content: ShareLinkContent = ShareLinkContent.Builder()
            .setContentUrl(Uri.parse("https://example.com"))
            .setQuote("Tole je pa kr kul stvarca")
            .build()

        binding.fbShareButton.shareContent = content
    }

    private fun setContentVideo() {
        val videoFileUri = getVideoUriFromRawResource(R.raw.milk, resources, getExternalFilesDir(DIRECTORY_MOVIES)!!)

        val video: ShareVideo = ShareVideo.Builder()
            .setLocalUrl(videoFileUri)
            .build()

        val content: ShareVideoContent = ShareVideoContent.Builder()
            .setVideo(video)
            .setContentTitle("A tub of white liquid.")
            .build()

        binding.fbShareButton.shareContent = content
    }

    private fun setContentStroy() {
        val videoFileUri = getVideoUriFromRawResource(R.raw.milk, resources, getExternalFilesDir(DIRECTORY_MOVIES)!!)

        val bitmap: Bitmap = resources.getDrawable(R.drawable.rafting).toBitmap()

        val video: ShareVideo = ShareVideo.Builder()
            .setLocalUrl(videoFileUri)
            .build()

        val photo: SharePhoto = SharePhoto.Builder()
            .setCaption("${demoApp.userName} has gone rafting!")
            .setBitmap(bitmap)
            .build()

        val content: ShareStoryContent = ShareStoryContent.Builder()
            .setBackgroundAsset(video)
            .setStickerAsset(photo)
            .build()

        binding.fbShareButton.shareContent = content
    }

    private fun setShareContent() {
        if(binding.switchPhoto.isChecked) {
            setContentPhoto()
        } else if(binding.switchLink.isChecked) {
            setContentLink()
        } else if(binding.switchVideo.isChecked) {
            setContentVideo()
        } else if(binding.switchStory.isChecked) {
            setContentStroy()
        }
    }

    private fun clickSwitchPhoto() {
        Log.i("SwitchPhoto", "Clicked on photo switch")
        binding.switchLink.isChecked = false
        binding.switchVideo.isChecked = false
        binding.switchStory.isChecked = false
        setShareContent()
    }

    private fun clickSwitchVideo() {
        Log.i("SwitchVideo", "Clicked on video switch")
        binding.switchLink.isChecked = false
        binding.switchPhoto.isChecked = false
        binding.switchStory.isChecked = false
        setShareContent()
    }

    private fun clickSwitchLink() {
        Log.i("SwitchLink", "Clicked on link switch")
        binding.switchPhoto.isChecked = false
        binding.switchVideo.isChecked = false
        binding.switchStory.isChecked = false
        setShareContent()
    }

    private fun clickSwitchStroy() {
        Log.i("SwitchStory", "Clicked on stroy switch")
        binding.switchPhoto.isChecked = false
        binding.switchVideo.isChecked = false
        binding.switchLink.isChecked = false
        setShareContent()
    }

    private fun clickShareReel() {
        val intent = Intent("com.facebook.stories.ADD_TO_STORY")

        val appId = getString(R.string.facebook_app_id) // This is your application's FB ID
        intent.putExtra("com.facebook.platform.extra.APPLICATION_ID", appId)

        val videoAssetUri = FileProvider.getUriForFile(baseContext, "pora.predstavitev.app.provider", getVideoFromRawResource(R.raw.milk, resources, getExternalFilesDir(DIRECTORY_MOVIES)!!)!!)
        intent.setDataAndType(videoAssetUri, "video/mp4")

        //val stickerAssetUri = FileProvider.getUriForFile(baseContext, "pora.predstavitev.app.provider", getImageFromDrawable(R.raw.dog)!!)
        //intent.putExtra("interactive_asset_uri", stickerAssetUri)

        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val activity: Activity = this

        if (activity.packageManager.resolveActivity(intent, 0) != null) {
            activity.startActivityForResult(intent, 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        demoApp = application as DemoApplication
        binding = ActivityMainBinding.inflate(layoutInflater)
        setShareContent()
        binding.switchLink.setOnClickListener { clickSwitchLink() }
        binding.switchPhoto.setOnClickListener { clickSwitchPhoto() }
        binding.switchVideo.setOnClickListener { clickSwitchVideo() }
        binding.switchStory.setOnClickListener { clickSwitchStroy() }
        binding.shareReelButton.setOnClickListener { clickShareReel() }

        accessTokenTracker = object : AccessTokenTracker() {
            override fun onCurrentAccessTokenChanged(
                oldAccessToken: AccessToken?,
                currentAccessToken: AccessToken?
            ) {
                if(currentAccessToken == null)
                    binding.loginText.text = "Logged out"
            }
        }

        LoginManager.getInstance().retrieveLoginStatus(this, object : LoginStatusCallback {
            override fun onCompleted(accessToken: AccessToken) {
                Log.i("AccessToken", "Got access token")
                this@MainActivity.demoApp.accessToken = accessToken

                val request = GraphRequest.newMeRequest(
                    accessToken
                ) { `object`: JSONObject?, _: GraphResponse? ->
                    println("About to try do the request")
                    try {
                        val name = `object`!!.getString("name")
                        val email = `object`.getString("email")
                        this@MainActivity.demoApp.userName = name
                        this@MainActivity.demoApp.userEmail = email
                        binding.loginText.text = "Logged in as: $name"
                    } catch (e: Exception) {
                        Log.e("RetrieveLogin", "Error fatching profile data")
                        e.printStackTrace()
                    }
                }

                val parameters = Bundle()
                parameters.putString("fields", "id,name,email")
                request.parameters = parameters
                request.executeAsync()
            }

            override fun onFailure() {
                Log.i("RetrieveLogin", "Failed to fetch access token")
            }

            override fun onError(exception: Exception) {
                Log.e("RetrieveLogin", "Error: ${exception.toString()}")
            }
        })

        binding.loginButton.setPermissions(EMAIL, PUBLIC_PROFILE)
        binding.loginButton.registerCallback(
            demoApp.callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    println("Succesfully loggedin")
                    demoApp.accessToken = result.accessToken

                    val request = GraphRequest.newMeRequest(
                        result.accessToken
                    ) { `object`: JSONObject?, _: GraphResponse? ->
                        try {
                            // Extract data from the JSON object
                            val name = `object`!!.getString("name")
                            val email = `object`.getString("email")
                            this@MainActivity.demoApp.userName = name
                            this@MainActivity.demoApp.userEmail = email
                            this@MainActivity.binding.loginText.text = "Logged in as: $name"
                        } catch (e: Exception) {
                            Log.e("ErrorSettings", "Error fatching profile data")
                            e.printStackTrace()
                        }
                    }

                    val parameters = Bundle()
                    parameters.putString("fields", "id,name,email")
                    request.parameters = parameters
                    request.executeAsync()
                }

                override fun onCancel() {
                    Log.i("LoginButtonCallback", "User canceled login")
                }

                override fun onError(error: FacebookException) {
                    Log.e("LoginButtonCallback", "error: ${error.toString()}")
                }
            })

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        accessTokenTracker.stopTracking()
    }
}