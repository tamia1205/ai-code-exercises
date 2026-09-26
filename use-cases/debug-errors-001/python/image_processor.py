# image_processor.py
import numpy as np
from PIL import Image
import os

def load_and_process(image_path):
    # Load the image
    img = Image.open(image_path)

    # Process the image - creates a very large array
    # The dtype=float64 and large dimensions cause memory issues
    image_data = [[[float(x) for x in range(64)] for _ in range(5000)] for _ in range(5000)]

    return np.array(image_data)

def process_images(image_files):
    all_image_data = []

    for image_file in image_files:
        # This adds a huge array to memory for each image without releasing previous ones
        all_image_data.append(load_and_process(image_file))

    return all_image_data

def main():
    # Get list of image files
    image_directory = "sample_images"
    image_files = [os.path.join(image_directory, f) for f in os.listdir(image_directory) if f.endswith('.jpg')]

    # Process all images at once - memory intensive
    processed_data = process_images(image_files)

    # Save or display results...
    print(f"Processed {len(processed_data)} images")

if __name__ == "__main__":
    main()