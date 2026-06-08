export type ImageCompressionOptions = {
  maxWidth: number;
  maxHeight: number;
  maxInputBytes: number;
  maxOutputBytes: number;
  quality?: number;
  minimumQuality?: number;
  outputType?: "image/jpeg" | "image/webp";
  acceptedTypes?: readonly string[];
};

const DEFAULT_ACCEPTED_TYPES = [
  "image/jpeg",
  "image/png",
  "image/webp",
] as const;

export class ImageCompressionError extends Error {
  constructor(message: string) {
    super(message);
    this.name = "ImageCompressionError";
  }
}

export class ImageCompressor {
  static async compress(
    file: File,
    options: ImageCompressionOptions,
  ): Promise<File> {
    const acceptedTypes = options.acceptedTypes ?? DEFAULT_ACCEPTED_TYPES;

    if (!acceptedTypes.includes(file.type)) {
      throw new ImageCompressionError("Use uma imagem JPEG, PNG ou WebP.");
    }

    if (file.size > options.maxInputBytes) {
      throw new ImageCompressionError(
        `A imagem original deve ter no máximo ${this.megabytes(options.maxInputBytes)} MB.`,
      );
    }

    const bitmap = await createImageBitmap(file).catch(() => {
      throw new ImageCompressionError("Não foi possível ler esta imagem.");
    });

    try {
      const dimensions = this.fitDimensions(
        bitmap.width,
        bitmap.height,
        options.maxWidth,
        options.maxHeight,
      );
      const canvas = document.createElement("canvas");
      canvas.width = dimensions.width;
      canvas.height = dimensions.height;

      const context = canvas.getContext("2d");
      if (!context) {
        throw new ImageCompressionError("Não foi possível processar a imagem.");
      }

      context.drawImage(bitmap, 0, 0, dimensions.width, dimensions.height);

      const outputType = options.outputType ?? "image/webp";
      const minimumQuality = options.minimumQuality ?? 0.55;
      let quality = options.quality ?? 0.82;
      let blob = await this.toBlob(canvas, outputType, quality);

      while (blob.size > options.maxOutputBytes && quality > minimumQuality) {
        quality = Math.max(minimumQuality, quality - 0.08);
        blob = await this.toBlob(canvas, outputType, quality);
      }

      if (blob.size > options.maxOutputBytes) {
        throw new ImageCompressionError(
          `Não foi possível reduzir a imagem para menos de ${this.megabytes(options.maxOutputBytes)} MB.`,
        );
      }

      return new File(
        [blob],
        `${this.filenameWithoutExtension(file.name)}.${this.extension(outputType)}`,
        {
          type: outputType,
          lastModified: Date.now(),
        },
      );
    } finally {
      bitmap.close();
    }
  }

  private static fitDimensions(
    width: number,
    height: number,
    maxWidth: number,
    maxHeight: number,
  ) {
    const scale = Math.min(maxWidth / width, maxHeight / height, 1);

    return {
      width: Math.max(1, Math.round(width * scale)),
      height: Math.max(1, Math.round(height * scale)),
    };
  }

  private static toBlob(
    canvas: HTMLCanvasElement,
    type: string,
    quality: number,
  ): Promise<Blob> {
    return new Promise((resolve, reject) => {
      canvas.toBlob((blob) => {
        if (blob) {
          resolve(blob);
          return;
        }

        reject(
          new ImageCompressionError("Não foi possível comprimir a imagem."),
        );
      }, type, quality);
    });
  }

  private static filenameWithoutExtension(filename: string) {
    return filename.replace(/\.[^/.]+$/, "") || "image";
  }

  private static extension(type: string) {
    return type === "image/jpeg" ? "jpg" : "webp";
  }

  private static megabytes(bytes: number) {
    return Math.round((bytes / 1024 / 1024) * 10) / 10;
  }
}
