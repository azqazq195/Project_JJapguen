package com.project.jjap_guen.adapter

import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.jjap_guen.R
import com.project.jjap_guen.model.Foret

class ForetAdapter : RecyclerView.Adapter<ForetAdapter.ForetViewHolder>() {
    inner class ForetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    val dumpImage: MutableList<String> = arrayListOf(
        "https://lh3.googleusercontent.com/proxy/YwsI5CG2LJQXDgjEM8QoLpi0lXq-eaNrZ-HGDM2WB_rVgrQtuUdchxq13u8ikcFT8l4LIu3EMVW4ogoa4YtgAq9FRuQrEAjdxCzuxVJIIWpBQd5Oj2ZTOwZ9rbL9OpZS9KOofCJ5k4Gq5Nls9_Fo_msGIbD8J9ibrSmCSpN3muV1iDh5mBh66OeWUDdvJGFsCxqSKIV09doXnEtfM_E7NurcjOvb39oEg5AS0GM46N4uQh3xWFYNOXK8Q1lFUfuJ3qmQnsJRr4BZX1pvUYloOTmdPN1k5Ubf3NgQDsPuBw",
        "https://lh3.googleusercontent.com/proxy/gNKfIhH6M6vJZIJLvS0-DeJZu5ujgA6u_7pD2Rlt9tiNBZphIt_NgmNux5P8vPnaoWGJOLQrlNML4GSs16_VFjyasFDTPSbR-MJr3p1oh_9J01JrmM78AZuAlvEPUkgofQgPww_JoNXiFy_XPjCU69IAI98UwIjZ563ZY29SDMe5GnhdTOYZV1yucrFrneESpCt63Xt6FMFiClWatCiEzPs5ejRvAcHRz5MgnQNw_-q1l2j7eTHoULAKK61V",
        "https://thumb.pann.com/tc_480/http://fimg4.pann.com/new/download.jsp?FileID=37825446"
    )


    private val differCallBack = object : DiffUtil.ItemCallback<Foret>() {
        override fun areItemsTheSame(oldItem: Foret, newItem: Foret): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Foret, newItem: Foret): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForetViewHolder {
        return ForetViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_foret_image,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ForetViewHolder, position: Int) {
        val foret = differ.currentList[position]
        holder.itemView.apply {
            Glide.with(this).load(
                // foret.photos[0].dir + "/" + foret.photos[0].filename
                dumpImage[position]
            ).into(holder.itemView.findViewById(R.id.ivForetImage))
            holder.itemView.findViewById<TextView>(R.id.textView).text = foret.name
//            holder.itemView.findViewById<TextView>(R.id.tvForetName).text = foret.name
            setOnClickListener {
                onItemClickListener?.let { it(foret) }
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private var onItemClickListener: ((Foret) -> Unit)? = null

    fun setOnItemClickListener(listener: (Foret) -> Unit) {
        onItemClickListener = listener
    }
}