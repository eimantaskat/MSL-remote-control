package minecraft.server.launcher.remote.control

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ConsoleRecyclerviewAdapter : RecyclerView.Adapter<ConsoleRecyclerviewAdapter.ViewHolder>() {

    private val consoleLines: MutableList<String> = mutableListOf()

    // Method to add data
    fun addData(data: String) {
        consoleLines.add(data)
        notifyItemInserted(consoleLines.size - 1)
    }

    fun clear() {
        val size: Int = consoleLines.size
        consoleLines.clear()
        notifyItemRangeRemoved(0, size)
    }

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_design, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemViewModel = consoleLines[position]
        // sets the text to the textview from our itemHolder class
        holder.textView.text = itemViewModel
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return consoleLines.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
    }
}